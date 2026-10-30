package ch.heigvd.amt.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/**
 * La validation en deux phases, PostgreSQL dans le rôle d'un participant. La transaction préparée
 * survit à sa connexion et garde ses verrous jusqu'à la décision du coordinateur.
 */
class PreparedTransactionTest {

    @BeforeEach
    void stock() throws SQLException {
        Postgres.resetTicketStock(10, 10, 10);
    }

    @Test
    void aPreparedTransactionOutlivesItsConnection() throws SQLException {
        prepare("reservation-42"); // phase 1 : le participant vote oui

        assertEquals("reservation-42", Postgres.queryValue("SELECT gid FROM pg_prepared_xacts"));
        assertEquals(10, Postgres.remaining("FRIDAY")); // pas encore validée
        var e = assertThrows(SQLException.class, () -> Postgres.execute("""
                SET lock_timeout = '100ms';
                UPDATE ticket_stock SET remaining = remaining - 1 WHERE day = 'FRIDAY'"""));
        assertEquals("55P03", e.getSQLState()); // la ligne reste verrouillée

        Postgres.execute("COMMIT PREPARED 'reservation-42'"); // phase 2, depuis une autre connexion
        assertEquals(8, Postgres.remaining("FRIDAY"));
    }

    @Test
    void theCoordinatorCanStillRollBack() throws SQLException {
        prepare("reservation-43");

        Postgres.execute("ROLLBACK PREPARED 'reservation-43'");
        assertEquals(10, Postgres.remaining("FRIDAY"));
    }

    /** Prépare une réservation de deux billets, puis ferme la connexion, comme si le coordinateur tombait. */
    private static void prepare(String id) throws SQLException {
        Postgres.execute("""
                BEGIN;
                UPDATE ticket_stock SET remaining = remaining - 2 WHERE day = 'FRIDAY';
                PREPARE TRANSACTION '%s'""".formatted(id));
    }
}
