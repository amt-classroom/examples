package ch.heigvd.amt.isolation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/**
 * Les anomalies d'isolation, rejouées sur PostgreSQL avec deux connexions, A et B. Chaque
 * scénario envoie ses requêtes dans l'ordre et rend ce que les transactions ont lu.
 */
class AnomaliesTest {

    Connection a;
    Connection b;

    @BeforeEach
    void openTwoConnections() throws SQLException {
        Postgres.execute("""
                DROP TABLE IF EXISTS accounts, post, orders, first_aid;
                CREATE TABLE accounts (id INT PRIMARY KEY, balance INT NOT NULL);
                INSERT INTO accounts VALUES (1, 500), (2, 500);
                CREATE TABLE post (id INT PRIMARY KEY, likes INT NOT NULL);
                INSERT INTO post VALUES (1, 1);
                CREATE TABLE orders (id SERIAL PRIMARY KEY, amount INT NOT NULL);
                INSERT INTO orders (amount) VALUES (120), (90), (300);
                CREATE TABLE first_aid (volunteer TEXT PRIMARY KEY, on_duty BOOLEAN NOT NULL);
                INSERT INTO first_aid VALUES ('Alice', true), ('Bob', true)""");
        a = Postgres.dataSource().getConnection();
        b = Postgres.dataSource().getConnection();
        a.setAutoCommit(false);
        b.setAutoCommit(false);
    }

    @AfterEach
    void closeConnections() throws SQLException {
        a.close(); // PostgreSQL annule une transaction restée ouverte
        b.close();
    }

    @Test
    void readCommittedIsTheDefault() throws SQLException {
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, a.getTransactionIsolation());
    }

    /** La lecture sale : B lit le compte 1 avant, pendant et après le virement de A. */
    @Test
    void readUncommittedReadsNoDirtyDataInPostgres() throws SQLException {
        b.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

        int before = read(b, "SELECT balance FROM accounts WHERE id = 1");
        update(a, "UPDATE accounts SET balance = balance + 100 WHERE id = 1");
        int during = read(b, "SELECT balance FROM accounts WHERE id = 1");
        update(a, "UPDATE accounts SET balance = balance - 100 WHERE id = 2");
        a.commit();
        int after = read(b, "SELECT balance FROM accounts WHERE id = 1");

        assertEquals(List.of(500, 500, 600), List.of(before, during, after)); // jamais 600 pendant
    }

    @Test
    void readCommittedAllowsANonRepeatableRead() throws SQLException {
        assertEquals(List.of(500, 600), nonRepeatableRead());
    }

    @Test
    void repeatableReadKeepsItsSnapshot() throws SQLException {
        a.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        assertEquals(List.of(500, 500), nonRepeatableRead());
    }

    @Test
    void readCommittedLosesALike() throws SQLException {
        assertEquals(2, lostUpdate()); // deux « j'aime » de plus, le compteur devrait valoir 3
    }

    @Test
    void repeatableReadRefusesTheLostUpdate() throws SQLException {
        a.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        b.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        var e = assertThrows(SQLException.class, this::lostUpdate);

        assertEquals("40001", e.getSQLState()); // l'UPDATE de B, sur une ligne que A a changée
        System.out.println(e.getMessage()); // could not serialize access due to concurrent update
    }

    @Test
    void readCommittedAllowsAPhantom() throws SQLException {
        assertEquals(List.of(2, 3), phantomRead());
    }

    @Test
    void repeatableReadPreventsThePhantomInPostgres() throws SQLException {
        a.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        assertEquals(List.of(2, 2), phantomRead());
    }

    @Test
    void repeatableReadAllowsWriteSkew() throws SQLException {
        a.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        b.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        assertEquals(0, writeSkew()); // personne de garde
    }

    @Test
    void serializableRefusesWriteSkew() throws SQLException {
        a.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        b.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        var e = assertThrows(SQLException.class, this::writeSkew);

        assertEquals("40001", e.getSQLState());
        System.out.println(e.getMessage());
    }

    /** La lecture non répétable : A lit le compte 1 avant et après le virement de B. */
    private List<Integer> nonRepeatableRead() throws SQLException {
        int first = read(a, "SELECT balance FROM accounts WHERE id = 1");
        update(b, "UPDATE accounts SET balance = balance + 100 WHERE id = 1");
        update(b, "UPDATE accounts SET balance = balance - 100 WHERE id = 2");
        b.commit();
        int second = read(a, "SELECT balance FROM accounts WHERE id = 1");
        return List.of(first, second);
    }

    /** La mise à jour perdue : A et B lisent le compteur, ajoutent 1 et l'écrivent. Rend le compteur. */
    private int lostUpdate() throws SQLException {
        int seenByA = read(a, "SELECT likes FROM post WHERE id = 1");
        int seenByB = read(b, "SELECT likes FROM post WHERE id = 1");
        update(a, "UPDATE post SET likes = " + (seenByA + 1) + " WHERE id = 1");
        a.commit();
        update(b, "UPDATE post SET likes = " + (seenByB + 1) + " WHERE id = 1");
        b.commit();
        return read(a, "SELECT likes FROM post WHERE id = 1");
    }

    /** La lecture fantôme : A compte deux fois les grosses commandes, B en insère une entre deux. */
    private List<Integer> phantomRead() throws SQLException {
        int first = read(a, "SELECT count(*) FROM orders WHERE amount > 100");
        update(b, "INSERT INTO orders (amount) VALUES (150)");
        b.commit();
        int second = read(a, "SELECT count(*) FROM orders WHERE amount > 100");
        return List.of(first, second);
    }

    /**
     * L'écriture croisée : il faut un secouriste de garde. Alice et Bob vérifient chacun qu'il en
     * reste plus d'un, puis s'en vont. Rend le nombre de secouristes restés de garde.
     */
    private int writeSkew() throws SQLException {
        int seenByAlice = read(a, "SELECT count(*) FROM first_aid WHERE on_duty");
        int seenByBob = read(b, "SELECT count(*) FROM first_aid WHERE on_duty");
        if (seenByAlice > 1) {
            update(a, "UPDATE first_aid SET on_duty = false WHERE volunteer = 'Alice'");
        }
        if (seenByBob > 1) {
            update(b, "UPDATE first_aid SET on_duty = false WHERE volunteer = 'Bob'");
        }
        a.commit();
        b.commit();
        return read(a, "SELECT count(*) FROM first_aid WHERE on_duty");
    }

    private static int read(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static void update(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
