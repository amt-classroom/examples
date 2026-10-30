package ch.heigvd.amt.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Deux billets pour vendredi et samedi, alors qu'il n'en reste qu'un samedi. */
class JdbcTransactionTest {

    @BeforeEach
    void saturdayAlmostSoldOut() throws SQLException {
        Postgres.resetTicketStock(10, 1, 10);
    }

    @Test
    void withoutTransactionFridayLosesItsTickets() throws SQLException {
        assertThrows(SoldOutException.class, () -> Reservations.reserveWithoutTransaction(
            Postgres.dataSource(), List.of("FRIDAY", "SATURDAY"), 2));

        assertEquals(8, Postgres.remaining("FRIDAY")); // deux billets que personne n'a
    }

    @Test
    void theRollbackGivesFridayItsTicketsBack() throws SQLException {
        assertThrows(SoldOutException.class, () -> Reservations.reserveDays(
            Postgres.dataSource(), List.of("FRIDAY", "SATURDAY"), 2));

        assertEquals(10, Postgres.remaining("FRIDAY"));
    }

    @Test
    void theCommitWritesEveryDay() throws SQLException, SoldOutException {
        Reservations.reserveDays(Postgres.dataSource(), List.of("FRIDAY", "SUNDAY"), 2);

        assertEquals(8, Postgres.remaining("FRIDAY"));
        assertEquals(8, Postgres.remaining("SUNDAY"));
    }

    /** La contrainte CHECK refuse un stock négatif, quel que soit le client. */
    @Test
    void theCheckConstraintRefusesANegativeStock() {
        var e = assertThrows(SQLException.class,
            () -> Postgres.execute("UPDATE ticket_stock SET remaining = remaining - 2 WHERE day = 'SATURDAY'"));

        assertEquals("23514", e.getSQLState()); // check_violation
        System.out.println(e.getMessage());
    }
}
