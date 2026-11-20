package ch.heigvd.amt.cdi.scopes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Un bean d'application qui utilise un bean de requête : le conteneur lui injecte un proxy, qui
 * retrouve à chaque appel l'acheteur de la requête en cours.
 */
@ApplicationScoped
public class ReservationService {
    @Inject CurrentBuyer buyer;
    @Inject Receipt receipt;
    @Inject Receipt copy;

    public CurrentBuyer buyer() {
        return buyer;
    }

    public Receipt receipt() {
        return receipt;
    }

    public Receipt copy() {
        return copy;
    }
}
