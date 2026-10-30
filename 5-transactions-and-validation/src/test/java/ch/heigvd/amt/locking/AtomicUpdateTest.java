package ch.heigvd.amt.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.heigvd.amt.Postgres;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** La mise à jour atomique : vérifier le stock et le décrémenter dans la même requête. */
@ExtendWith(Postgres.class)
@HelidonTest
class AtomicUpdateTest {

    @Inject
    StockService stocks;

    @Test
    void theUpdateReservesOrRefuses() throws SQLException {
        Postgres.resetTicketStock(4, 10, 10);

        assertEquals(1, stocks.reserveAtomically("FRIDAY", 3));
        assertEquals(0, stocks.reserveAtomically("FRIDAY", 3)); // il n'en reste qu'un

        assertEquals(1, Postgres.remaining("FRIDAY"));
    }
}
