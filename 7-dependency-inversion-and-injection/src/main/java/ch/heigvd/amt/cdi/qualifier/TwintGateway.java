package ch.heigvd.amt.cdi.qualifier;

import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;

/** Le bean que désigne le qualificateur {@code @Twint}. */
@Twint
@ApplicationScoped
public class TwintGateway implements PaymentGateway {
    public boolean supports(String method) {
        return method.equals("twint");
    }

    public boolean charge(String email, int amount) {
        return true;
    }
}
