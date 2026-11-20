package ch.heigvd.amt.cdi.named;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** Chaque point d'injection désigne son bean par la même chaîne que lui. */
@ApplicationScoped
public class Checkout {
    @Inject @Named("card") PaymentGateway card;
    @Inject @Named("twint") PaymentGateway twint;

    public PaymentGateway card() {
        return card;
    }

    public PaymentGateway twint() {
        return twint;
    }
}
