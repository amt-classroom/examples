package ch.heigvd.amt.festival;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Le service de réservation confirme chaque réservation par e-mail. */
@ApplicationScoped
public class ReservationService {
    @Inject
    Mailer mailer;

    public void reserve(String email, int quantity) {
        mailer.send(email, quantity + " tickets reserved");
    }
}
