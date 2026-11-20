package ch.heigvd.amt.cdi.qualifier;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Chaque point d'injection désigne son bean par une annotation, que le compilateur vérifie. */
@ApplicationScoped
public class Checkout {
    @Inject @Card PaymentGateway card;
    @Inject @Twint PaymentGateway twint;

    public PaymentGateway card() {
        return card;
    }

    public PaymentGateway twint() {
        return twint;
    }
}
