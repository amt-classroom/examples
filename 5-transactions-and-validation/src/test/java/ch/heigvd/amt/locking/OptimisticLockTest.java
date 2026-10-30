package ch.heigvd.amt.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

/** Le verrouillage optimiste : deux organisateurs modifient la fiche du même concert. */
class OptimisticLockTest {

    @Test
    void theSecondWriterFails() {
        try (EntityManagerFactory factory = Postgres.entityManagerFactory("concerts", Map.of())) {
            var concert = new Concert("The Grand Canal");
            try (EntityManager em = factory.createEntityManager()) {
                em.getTransaction().begin();
                em.persist(concert);
                em.getTransaction().commit();
            }

            try (EntityManager alice = factory.createEntityManager();
                    EntityManager bob = factory.createEntityManager()) {
                alice.getTransaction().begin();
                bob.getTransaction().begin();
                Concert hers = alice.find(Concert.class, concert.getId()); // version 0
                Concert his = bob.find(Concert.class, concert.getId());    // version 0

                hers.setDescription("Post-rock from Venice");
                alice.getTransaction().commit(); // update ... where id=? and version=0
                assertEquals(1, hers.getVersion());

                his.setDescription("Shoegaze from Yverdon");
                var e = assertThrows(RollbackException.class, () -> bob.getTransaction().commit());
                assertInstanceOf(OptimisticLockException.class, e.getCause()); // aucune ligne en version 0
                System.out.println(e.getCause().getMessage());
            }
        }
    }
}
