package ch.heigvd.amt.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/**
 * Le renommage d'une table : une vue garde l'ancien nom pendant la transition. PostgreSQL peut
 * écrire à travers une vue simple, et l'ancien code continue sans rien savoir.
 */
class RenameTableWithViewTest {

    @BeforeEach
    void expand() throws SQLException {
        Postgres.emptyDatabase();
        Postgres.execute("""
                CREATE TABLE cust_tb_prod (id serial PRIMARY KEY, name text NOT NULL);
                INSERT INTO cust_tb_prod (name) VALUES ('Ada Lovelace')""");

        Postgres.execute("""
                ALTER TABLE cust_tb_prod RENAME TO customer;
                CREATE VIEW cust_tb_prod AS SELECT * FROM customer""");
    }

    @Test
    void theOldCodeWritesThroughTheView() throws SQLException {
        Postgres.execute("INSERT INTO cust_tb_prod (name) VALUES ('Grace Hopper')");
        Postgres.execute("UPDATE cust_tb_prod SET name = 'Ada King' WHERE id = 1");

        assertEquals(2L, Postgres.queryValue("SELECT count(*) FROM customer"));
        assertEquals("Ada King", Postgres.queryValue("SELECT name FROM customer WHERE id = 1"));
    }

    @Test
    void theOldCodeSeesWhatTheNewCodeWrites() throws SQLException {
        Postgres.execute("INSERT INTO customer (name) VALUES ('Grace Hopper')");

        assertEquals(2L, Postgres.queryValue("SELECT count(*) FROM cust_tb_prod"));
    }

    @Test
    void theContractDropsTheViewAndKeepsTheData() throws SQLException {
        Postgres.execute("DROP VIEW cust_tb_prod");

        assertEquals(1L, Postgres.queryValue("SELECT count(*) FROM customer"));
        var e = assertThrows(SQLException.class, () -> Postgres.queryValue("SELECT count(*) FROM cust_tb_prod"));
        assertTrue(e.getMessage().contains("relation \"cust_tb_prod\" does not exist"), e.getMessage());
    }
}
