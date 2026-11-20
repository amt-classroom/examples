package ch.heigvd.amt.injection.setter;

import ch.heigvd.amt.festival.Mailer;

/** Une dépendance facultative : tant que personne n'appelle le setter, le champ vaut null. */
public class ReservationService {
    private Mailer mailer;

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public void reserve(String email, int quantity) {
        if (mailer != null) {
            mailer.send(email, quantity + " tickets reserved");
        }
    }
}
