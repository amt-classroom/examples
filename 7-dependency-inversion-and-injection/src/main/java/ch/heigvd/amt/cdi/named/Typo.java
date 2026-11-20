package ch.heigvd.amt.cdi.named;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** Une faute de frappe dans la chaîne compile : seul le conteneur la signale, au démarrage. */
@ApplicationScoped
public class Typo {
    @Inject @Named("crad") PaymentGateway card;
}
