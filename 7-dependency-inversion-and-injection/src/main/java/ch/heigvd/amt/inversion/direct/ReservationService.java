package ch.heigvd.amt.inversion.direct;

/** Le module de haut niveau crée lui-même le module de bas niveau : il en dépend directement. */
class ReservationService {
    private final SmtpMailer mailer = new SmtpMailer();

    void reserve(String email, int quantity) {
        mailer.send(email, quantity + " tickets reserved");
    }
}
