package ch.heigvd.amt.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** La contrainte d'unicité : la même demande envoyée deux fois, par un double clic. */
class UniqueConstraintTest {

    static final String INSERT = """
            INSERT INTO reservation_request (idempotency_key, email, quantity)
            VALUES ('9f1c2e4a-7b3d-4c8e-a1f0-5d6e7f8a9b0c', 'jane@example.com', 2)""";

    static final String INSERT_OR_NOTHING = INSERT + " ON CONFLICT (idempotency_key) DO NOTHING";

    @BeforeEach
    void table() throws SQLException {
        Postgres.execute("""
                DROP TABLE IF EXISTS reservation_request;
                CREATE TABLE reservation_request (
                    id              BIGSERIAL PRIMARY KEY,
                    idempotency_key UUID NOT NULL UNIQUE,
                    email           VARCHAR(255) NOT NULL,
                    quantity        INT NOT NULL
                )""");
    }

    @Test
    void theSecondInsertDoesNothing() throws SQLException {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            assertEquals(1, update(connection, INSERT_OR_NOTHING));
            assertEquals(0, update(connection, INSERT_OR_NOTHING));
        }
    }

    @Test
    void withoutOnConflictTheSecondInsertFails() throws SQLException {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            update(connection, INSERT);
            var e = assertThrows(SQLException.class, () -> update(connection, INSERT));

            assertEquals("23505", e.getSQLState()); // unique_violation
            System.out.println(e.getMessage());
        }
    }

    /** Deux demandes simultanées : la seconde attend que la première valide, puis ne fait rien. */
    @Test
    void theConcurrentInsertWaitsThenDoesNothing() throws Exception {
        try (ExecutorService executor = Executors.newSingleThreadExecutor();
                Connection first = Postgres.dataSource().getConnection();
                Connection second = Postgres.dataSource().getConnection()) {
            first.setAutoCommit(false);
            assertEquals(1, update(first, INSERT_OR_NOTHING));

            Future<Integer> secondInsert = executor.submit(() -> update(second, INSERT_OR_NOTHING));
            Thread.sleep(300);
            assertFalse(secondInsert.isDone()); // la seconde attend sur l'index unique

            first.commit();
            assertEquals(0, secondInsert.get(5, TimeUnit.SECONDS));
        }
    }

    private static int update(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }
}
