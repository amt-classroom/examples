package ch.heigvd.amt.cdi.named;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/** Le bean que désigne la chaîne « card ». */
@Named("card")
@ApplicationScoped
public class CardGateway implements PaymentGateway {
    public boolean supports(String method) {
        return method.equals("card");
    }

    public boolean charge(String email, int amount) {
        return true;
    }
}
