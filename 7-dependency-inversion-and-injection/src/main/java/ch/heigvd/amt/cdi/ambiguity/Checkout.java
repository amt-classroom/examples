package ch.heigvd.amt.cdi.ambiguity;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Un point d'injection que deux beans peuvent satisfaire : le conteneur refuse de démarrer. */
@ApplicationScoped
public class Checkout {
    @Inject
    PaymentGateway gateway;   // laquelle ?
}
