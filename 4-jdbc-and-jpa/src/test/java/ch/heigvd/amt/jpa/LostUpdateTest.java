package ch.heigvd.amt.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;

/**
 * La mise à jour perdue : des billets sont rendus entre la lecture et l'écriture d'une
 * réservation. {@code em} fait la réservation.
 */
class LostUpdateTest extends TicketingTest {

    @BeforeEach
    void fourTicketsLeft() {
        em.getTransaction().begin();
        em.merge(new TicketStock(1L, 4)); // insère la ligne, ou la remet à 4
        em.getTransaction().commit();
        em.clear();
    }

    @Test
    void aWriteBetweenTheReadAndTheWriteIsLost() {
        int quantity = 4;

        em.getTransaction().begin();
        TicketStock stock = em.find(TicketStock.class, 1L); // lit 4

        releaseTwoTickets(); // 6 en base

        if (stock.getRemaining() >= quantity) {
            stock.setRemaining(stock.getRemaining() - quantity);
        }
        em.getTransaction().commit(); // écrit 4 − 4 = 0

        assertEquals(0, stored(TicketStock.class, 1L).getRemaining()); // il devrait en rester 2
    }

    /** La solution en JPQL : la condition et l'écriture forment une seule requête. */
    @Test
    void theConditionalUpdateKeepsTheReleasedTickets() {
        em.getTransaction().begin();
        releaseTwoTickets(); // 6 en base

        assertEquals(1, reserve(4)); // la base calcule 6 − 4
        assertEquals(0, reserve(4)); // il en reste 2 : aucune ligne ne remplit la condition
        em.getTransaction().commit();

        assertEquals(2, stored(TicketStock.class, 1L).getRemaining());
    }

    /** Le revers : l'UPDATE contourne le contexte de persistance, et l'entité chargée reste périmée. */
    @Test
    void theBulkUpdateLeavesALoadedEntityStale() {
        em.getTransaction().begin();
        TicketStock stock = em.find(TicketStock.class, 1L);

        reserve(3);

        assertEquals(4, stock.getRemaining()); // périmée : la base en compte 1
        em.refresh(stock);
        assertEquals(1, stock.getRemaining());
        em.getTransaction().commit();
    }

    private int reserve(int quantity) {
        return em.createQuery("""
                UPDATE TicketStock s SET s.remaining = s.remaining - :q
                WHERE s.remaining >= :q
                """)
            .setParameter("q", quantity)
            .executeUpdate();
    }

    /** Rend deux billets, dans une transaction validée aussitôt. */
    private static void releaseTwoTickets() {
        try (EntityManager releaser = factory.createEntityManager()) {
            releaser.getTransaction().begin();
            TicketStock stock = releaser.find(TicketStock.class, 1L);
            stock.setRemaining(stock.getRemaining() + 2);
            releaser.getTransaction().commit();
        }
    }
}
