package ch.heigvd.amt.locking;

import ch.heigvd.amt.transactions.SoldOutException;
import ch.heigvd.amt.transactions.TicketStock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/** Deux manières de réserver sans perdre de mise à jour : verrouiller la ligne, ou tout faire en une requête. */
@ApplicationScoped
public class StockService {

    @PersistenceContext
    EntityManager em;

    @Transactional(rollbackOn = SoldOutException.class)
    public void reserve(String day, int quantity)
            throws SoldOutException {
        TicketStock stock = em.find(TicketStock.class, day,
                LockModeType.PESSIMISTIC_WRITE);
        // select ... from ticket_stock where day=?
        //     for no key update
        if (stock.getRemaining() < quantity) {
            throw new SoldOutException(day);
        }
        stock.setRemaining(stock.getRemaining() - quantity);
    }

    @Transactional
    public int reserveAtomically(String day, int quantity) {
        return em.createQuery("""
                UPDATE TicketStock s
                SET s.remaining = s.remaining - :q
                WHERE s.day = :day AND s.remaining >= :q
                """)
            .setParameter("q", quantity)
            .setParameter("day", day)
            .executeUpdate(); // 1 : réservé, 0 : complet
    }
}
