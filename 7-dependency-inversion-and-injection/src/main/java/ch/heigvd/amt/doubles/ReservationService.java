package ch.heigvd.amt.doubles;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * La classe sous test : elle débite l'acheteur, puis confirme par e-mail. Ses deux
 * dépendances arrivent par le constructeur, ce qui permet à un test de passer les siennes.
 */
@ApplicationScoped
public class ReservationService {
    private static final int PRICE = 90;
    private final PaymentGateway gateway;
    private final Mailer mailer;

    @Inject
    public ReservationService(PaymentGateway gateway, Mailer mailer) {
        this.gateway = gateway;
        this.mailer = mailer;
    }

    public boolean reserve(String email, int quantity) {
        if (!gateway.charge(email, quantity * PRICE)) {
            return false;
        }
        mailer.send(email, quantity + " tickets reserved");
        return true;
    }
}
