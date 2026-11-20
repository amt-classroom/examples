package ch.heigvd.amt.inversion;

/** Le module de haut niveau ne connaît plus que l'abstraction, qu'il reçoit par son constructeur. */
class ReservationService {
    private final Mailer mailer;

    ReservationService(Mailer mailer) {
        this.mailer = mailer;
    }

    void reserve(String email, int quantity) {
        mailer.send(email, quantity + " tickets reserved");
    }
}
