package ch.heigvd.amt.transactions;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Réserver plusieurs jours, la transaction confiée au conteneur avec @Transactional. */
@ApplicationScoped
public class ReservationService {
    @Inject TicketStocks stocks;

    @Transactional
    public void reserve(List<String> days, int quantity)
            throws SoldOutException {
        for (String day : days) {
            if (stocks.reserve(day, quantity) == 0) {
                // vérifiée : la transaction est validée
                throw new SoldOutException(day);
            }
        }
    }

    @Transactional(rollbackOn = SoldOutException.class)
    public void reserveAllOrNothing(List<String> days, int quantity) throws SoldOutException {
        for (String day : days) {
            if (stocks.reserve(day, quantity) == 0) {
                throw new SoldOutException(day); // annule la transaction
            }
        }
    }
}
