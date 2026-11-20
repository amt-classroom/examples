package ch.heigvd.amt.cdi.ambiguity;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;

/** Un premier bean de type PaymentGateway. */
@ApplicationScoped
public class CardGateway implements PaymentGateway {
    public boolean supports(String method) {
        return method.equals("card");
    }

    public boolean charge(String email, int amount) {
        return true;
    }
}
