package ch.heigvd.amt.jpa;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Le métier : il demande, l'EntityManager écrit le SQL. Aucun appel à save ni à update, le
 * contexte de persistance écrit ce qui a changé.
 */
@ApplicationScoped
public class ConcertService {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void schedule(Concert concert) {
        em.persist(concert); // INSERT, au plus tard au commit
    }

    public Concert find(long id) {
        return em.find(Concert.class, id); // SELECT ... WHERE id = ?
    }

    @Transactional
    public void reschedule(long id, LocalDateTime startsAt) {
        Concert concert = em.find(Concert.class, id); // SELECT
        concert.setStartsAt(startsAt);                // rien encore
    } // commit : flush, puis UPDATE concert SET ... WHERE id = ?

    @Transactional
    public void cancel(long id) {
        Concert concert = em.find(Concert.class, id);
        em.remove(concert); // DELETE au flush
    }
}
