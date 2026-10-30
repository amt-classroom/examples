package ch.heigvd.amt.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Les délais d'attente de PostgreSQL, et l'erreur que chacun produit. */
class TimeoutTest {

    @Test
    void lockTimeout() throws SQLException {
        Postgres.resetTicketStock(10, 10, 10);
        try (Connection holder = Postgres.dataSource().getConnection();
                Connection waiter = Postgres.dataSource().getConnection()) {
            holder.setAutoCommit(false);
            execute(holder, "UPDATE ticket_stock SET remaining = remaining - 1 WHERE day = 'FRIDAY'");

            waiter.setAutoCommit(false);
            execute(waiter, "SET LOCAL lock_timeout = '100ms'");
            var e = assertThrows(SQLException.class,
                () -> execute(waiter, "UPDATE ticket_stock SET remaining = remaining - 1 WHERE day = 'FRIDAY'"));

            assertEquals("55P03", e.getSQLState()); // lock_not_available
            System.out.println(e.getMessage());
        }
    }

    @Test
    void statementTimeout() throws SQLException {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            connection.setAutoCommit(false);
            execute(connection, "SET LOCAL statement_timeout = '100ms'");
            var e = assertThrows(SQLException.class, () -> execute(connection, "SELECT pg_sleep(1)"));

            assertEquals("57014", e.getSQLState()); // query_canceled
            System.out.println(e.getMessage());
        }
    }

    @Test
    void idleInTransactionTimeout() throws Exception {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            execute(connection, "SET idle_in_transaction_session_timeout = '100ms'");
            connection.setAutoCommit(false);
            execute(connection, "SELECT 1");
            Thread.sleep(500); // la transaction reste ouverte sans rien faire
            var e = assertThrows(SQLException.class, () -> execute(connection, "SELECT 1"));

            assertEquals("25P03", e.getSQLState()); // PostgreSQL a fermé la session
            System.out.println(e.getMessage());
        }
    }

    @Test
    void transactionTimeout() throws SQLException {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            execute(connection, "SET transaction_timeout = '200ms'");
            connection.setAutoCommit(false);
            var e = assertThrows(SQLException.class, () -> execute(connection, "SELECT pg_sleep(1)"));

            assertEquals("25P04", e.getSQLState()); // PostgreSQL a fermé la session
            System.out.println(e.getMessage());
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
