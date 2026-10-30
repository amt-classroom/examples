package ch.heigvd.amt.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.heigvd.amt.Postgres;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

/** Hibernate valide l'entité au flush, avant l'INSERT, et reporte ses contraintes dans le schéma. */
@ExtendWith(Postgres.class)
@HelidonTest
@AddBean(EntityValidationTest.ReservationRepository.class)
class EntityValidationTest {

    @Inject
    ReservationRepository reservations;

    @Test
    void anInvalidEntityIsNeverInserted() throws SQLException {
        // persist ne lève rien : le flush, au commit, valide l'entité et Narayana annule tout.
        var e = assertThrows(RollbackException.class, () -> reservations.save(new Reservation("jane@example.com", 6)));
        assertInstanceOf(ConstraintViolationException.class, e.getCause());

        assertEquals(0L, Postgres.queryValue("SELECT count(*) FROM Reservation"));
    }

    @Test
    void theSchemaCarriesTheConstraints() throws SQLException {
        String check = (String) Postgres.queryValue("""
                SELECT pg_get_constraintdef(oid) FROM pg_constraint
                WHERE conrelid = 'reservation'::regclass AND contype = 'c'""");
        System.out.println(check);
        assertTrue(check.contains("quantity <= 4") && check.contains("quantity >= 1"), check); // @Min, @Max

        assertEquals("NO", Postgres.queryValue("""
                SELECT is_nullable FROM information_schema.columns
                WHERE table_name = 'reservation' AND column_name = 'email'""")); // @NotNull
    }

    @ApplicationScoped
    static class ReservationRepository {

        @PersistenceContext
        EntityManager em;

        @Transactional
        public void save(Reservation reservation) {
            em.persist(reservation);
        }
    }
}
