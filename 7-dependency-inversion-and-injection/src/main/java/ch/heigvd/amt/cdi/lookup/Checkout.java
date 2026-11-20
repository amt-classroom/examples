package ch.heigvd.amt.cdi.lookup;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Notre code choisit le bean pendant l'exécution. {@code @Any} demande tous les beans du type,
 * quel que soit leur qualifier.
 */
@ApplicationScoped
public class Checkout {
    @Inject @Any
    Instance<PaymentGateway> gateways;

    public boolean pay(String method, String email, int amount) {
        for (PaymentGateway gateway : gateways) {
            if (gateway.supports(method)) {
                return gateway.charge(email, amount);
            }
        }
        throw new IllegalArgumentException("No gateway for " + method);
    }
}
