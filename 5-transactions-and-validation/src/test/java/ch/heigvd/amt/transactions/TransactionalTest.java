package ch.heigvd.amt.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.heigvd.amt.Postgres;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * @Transactional dans Helidon, quand il ne reste qu'un billet samedi. Narayana valide la
 * transaction sur une exception vérifiée, et l'annule sur une RuntimeException.
 */
@ExtendWith(Postgres.class)
@HelidonTest
@AddBean(TransactionalTest.FailingReservation.class)
class TransactionalTest {

    @Inject
    ReservationService service;

    @Inject
    FailingReservation failing;

    @BeforeEach
    void saturdayAlmostSoldOut() throws SQLException {
        Postgres.resetTicketStock(10, 1, 10);
    }

    @Test
    void theSuccessfulMethodCommits() throws SoldOutException, SQLException {
        service.reserve(List.of("FRIDAY", "SUNDAY"), 2);

        assertEquals(8, Postgres.remaining("FRIDAY"));
        assertEquals(8, Postgres.remaining("SUNDAY"));
    }

    @Test
    void aCheckedExceptionCommits() throws SQLException {
        assertThrows(SoldOutException.class, () -> service.reserve(List.of("FRIDAY", "SATURDAY"), 2));

        assertEquals(8, Postgres.remaining("FRIDAY")); // vendredi reste réservé
    }

    @Test
    void rollbackOnRollsBack() throws SQLException {
        assertThrows(SoldOutException.class, () -> service.reserveAllOrNothing(List.of("FRIDAY", "SATURDAY"), 2));

        assertEquals(10, Postgres.remaining("FRIDAY"));
    }

    @Test
    void aRuntimeExceptionRollsBack() throws SQLException {
        assertThrows(IllegalStateException.class, () -> failing.reserveFridayThenFail());

        assertEquals(10, Postgres.remaining("FRIDAY"));
    }

    /** Réserve vendredi, puis échoue comme si la base tombait. */
    @ApplicationScoped
    static class FailingReservation {

        @Inject
        TicketStocks stocks;

        @Transactional
        public void reserveFridayThenFail() {
            stocks.reserve("FRIDAY", 2);
            throw new IllegalStateException("database unavailable");
        }
    }
}
