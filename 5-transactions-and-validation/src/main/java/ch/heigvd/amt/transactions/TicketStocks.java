package ch.heigvd.amt.transactions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Le stock de chaque jour. La requête s'exécute dans la transaction de l'appelant. */
@ApplicationScoped
public class TicketStocks {

    @PersistenceContext
    EntityManager em;

    /** Retire les billets s'il en reste assez : rend 1, ou 0 si le jour est complet. */
    public int reserve(String day, int quantity) {
        return em.createQuery("""
                UPDATE TicketStock s
                SET s.remaining = s.remaining - :q
                WHERE s.day = :day AND s.remaining >= :q
                """)
            .setParameter("q", quantity)
            .setParameter("day", day)
            .executeUpdate();
    }
}
