package ch.heigvd.amt.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Le cycle de vie d'une entité : ce que le contexte suit, et ce qu'il laisse filer. */
class LifecycleTest extends TicketingTest {

    private Concert concert; // inséré par un autre EntityManager, donc détaché ici

    @BeforeEach
    void insert() {
        concert = new Concert("The Grand Canal", at(21, 0));
        Postgres.insert(factory, concert);
    }

    @Test
    void aNewEntityIsNotManaged() {
        assertFalse(em.contains(new Concert("Menhirs", at(19, 30))));
    }

    @Test
    void persistFindAndQueriesManageTheirEntities() {
        em.getTransaction().begin();
        var scheduled = new Concert("Menhirs", at(19, 30));
        em.persist(scheduled);
        Concert found = em.find(Concert.class, concert.getId());
        List<Concert> queried = em.createQuery("SELECT c FROM Concert c", Concert.class).getResultList();

        assertTrue(em.contains(scheduled));
        assertTrue(em.contains(found));
        assertTrue(queried.stream().allMatch(em::contains));
    }

    @Test
    void aDetachedEntityLosesItsChanges() {
        em.getTransaction().begin();
        Concert found = em.find(Concert.class, concert.getId());
        em.detach(found);
        found.setStartsAt(at(22, 0));
        em.getTransaction().commit();

        assertEquals(at(21, 0), stored(Concert.class, concert.getId()).getStartsAt());
    }

    /** Le piège : merge rend une copie gérée, et son argument reste détaché. */
    @Test
    void mergeReturnsAManagedCopy() {
        em.getTransaction().begin();
        Concert managed = em.merge(concert);
        concert.setStartsAt(at(22, 0)); // sur l'argument, après le merge : perdu
        em.getTransaction().commit();

        assertNotSame(concert, managed);
        assertEquals(at(21, 0), stored(Concert.class, concert.getId()).getStartsAt());
    }
}
