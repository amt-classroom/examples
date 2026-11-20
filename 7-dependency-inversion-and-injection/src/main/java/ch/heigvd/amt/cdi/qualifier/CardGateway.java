package ch.heigvd.amt.cdi.qualifier;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;

/** Le bean que désigne le qualifier {@code @Card}. */
@Card
@ApplicationScoped
public class CardGateway implements PaymentGateway {
    public boolean supports(String method) {
        return method.equals("card");
    }

    public boolean charge(String email, int amount) {
        return true;
    }
}
