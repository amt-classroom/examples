package ch.heigvd.amt.injection.container;

import ch.heigvd.amt.festival.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Le conteneur joue l'injecteur : il crée le service et remplit le champ. Personne n'écrit new. */
@ApplicationScoped
public class ReservationService {
    @Inject
    Mailer mailer;

    public void reserve(String email, int quantity) {
        mailer.send(email, quantity + " tickets reserved");
    }
}
