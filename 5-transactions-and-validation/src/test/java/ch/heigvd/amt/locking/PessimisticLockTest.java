package ch.heigvd.amt.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.heigvd.amt.Postgres;
import ch.heigvd.amt.transactions.SoldOutException;
import ch.heigvd.amt.transactions.TicketStock;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

/**
 * Le verrouillage pessimiste dans Helidon. Il reste quatre billets vendredi ; un client se fait
 * rembourser deux billets pendant qu'un autre réserve les quatre.
 */
@ExtendWith(Postgres.class)
@HelidonTest
@AddBean(PessimisticLockTest.Refunds.class)
class PessimisticLockTest {

    @Inject
    StockService stocks;

    @Inject
    Refunds refunds;

    @Inject
    TransactionManager transactionManager;

    @BeforeEach
    void fourTicketsLeft() throws SQLException {
        Postgres.resetTicketStock(4, 10, 10);
    }

    @Test
    void notEnoughTicketsIsRefused() throws SQLException {
        assertThrows(SoldOutException.class, () -> stocks.reserve("FRIDAY", 5));

        assertEquals(4, Postgres.remaining("FRIDAY"));
    }

    @Test
    void theRefundWaitsForTheReservation() throws Exception {
        transactionManager.begin(); // reserve rejoint cette transaction
        stocks.reserve("FRIDAY", 4); // lit 4, verrouille la ligne

        var refund = CompletableFuture.runAsync(refunds::refundTwo);
        Thread.sleep(300);
        assertFalse(refund.isDone()); // le remboursement attend le verrou

        transactionManager.commit(); // écrit 0, libère la ligne
        refund.get(5, TimeUnit.SECONDS); // lit 0, écrit 2

        assertEquals(2, Postgres.remaining("FRIDAY"));
    }

    @Test
    void aZeroTimeoutFailsInsteadOfWaiting() throws Exception {
        transactionManager.begin();
        try {
            stocks.reserve("FRIDAY", 1);

            var refund = CompletableFuture.runAsync(refunds::refundTwoWithoutWaiting);
            var e = assertThrows(ExecutionException.class, () -> refund.get(5, TimeUnit.SECONDS));

            assertInstanceOf(LockTimeoutException.class, e.getCause()); // for no key update nowait
        } finally {
            transactionManager.rollback();
        }
    }

    /** Rend deux billets de vendredi, dans sa propre transaction. */
    @ApplicationScoped
    static class Refunds {

        @PersistenceContext
        EntityManager em;

        @Transactional
        public void refundTwo() {
            TicketStock stock = em.find(TicketStock.class, "FRIDAY", LockModeType.PESSIMISTIC_WRITE);
            stock.setRemaining(stock.getRemaining() + 2);
        }

        @Transactional
        public void refundTwoWithoutWaiting() {
            TicketStock stock = em.find(TicketStock.class, "FRIDAY", LockModeType.PESSIMISTIC_WRITE,
                Map.of("jakarta.persistence.lock.timeout", 0));
            stock.setRemaining(stock.getRemaining() + 2);
        }
    }
}
