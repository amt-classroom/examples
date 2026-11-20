package ch.heigvd.amt.cdi.ambiguity;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;

/** Un second bean du même type, sans rien qui le distingue du premier. */
@ApplicationScoped
public class TwintGateway implements PaymentGateway {
    public boolean supports(String method) {
        return method.equals("twint");
    }

    public boolean charge(String email, int amount) {
        return true;
    }
}
