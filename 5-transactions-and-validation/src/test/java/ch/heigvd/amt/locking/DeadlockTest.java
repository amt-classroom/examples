package ch.heigvd.amt.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;
import ch.heigvd.amt.transactions.Reservations;

/** L'interblocage : deux réservations de vendredi et samedi, dans un ordre ou dans l'autre. */
class DeadlockTest {

    @BeforeEach
    void stock() throws SQLException {
        Postgres.resetTicketStock(10, 10, 10);
    }

    @Test
    void oppositeOrdersDeadlock() throws Exception {
        try (ExecutorService executor = Executors.newSingleThreadExecutor();
                Connection first = Postgres.dataSource().getConnection();
                Connection second = Postgres.dataSource().getConnection()) {
            first.setAutoCommit(false);
            second.setAutoCommit(false);
            Reservations.reserve(first, "FRIDAY", 2);    // verrouille vendredi
            Reservations.reserve(second, "SATURDAY", 2); // verrouille samedi

            Future<Integer> firstWaits = executor.submit(() -> Reservations.reserve(first, "SATURDAY", 2));
            Thread.sleep(200);
            // Chacune attend l'autre. Après deadlock_timeout (1 s), PostgreSQL annule la première.
            assertEquals(1, Reservations.reserve(second, "FRIDAY", 2));
            second.commit();

            var e = assertThrows(ExecutionException.class, firstWaits::get);
            SQLException deadlock = (SQLException) e.getCause();
            assertEquals("40P01", deadlock.getSQLState()); // deadlock_detected
            System.out.println(deadlock.getMessage());
        }
        assertEquals(8, Postgres.remaining("FRIDAY"));
        assertEquals(8, Postgres.remaining("SATURDAY"));
    }

    @Test
    void theSameOrderOnlyWaits() throws Exception {
        try (ExecutorService executor = Executors.newSingleThreadExecutor();
                Connection first = Postgres.dataSource().getConnection();
                Connection second = Postgres.dataSource().getConnection()) {
            first.setAutoCommit(false);
            second.setAutoCommit(false);
            Reservations.reserve(first, "FRIDAY", 2);

            Future<Integer> secondWaits = executor.submit(() -> Reservations.reserve(second, "FRIDAY", 2));
            Reservations.reserve(first, "SATURDAY", 2);
            first.commit(); // libère vendredi

            assertEquals(1, secondWaits.get(5, TimeUnit.SECONDS));
            Reservations.reserve(second, "SATURDAY", 2);
            second.commit();
        }
        assertEquals(6, Postgres.remaining("FRIDAY"));
        assertEquals(6, Postgres.remaining("SATURDAY"));
    }
}
