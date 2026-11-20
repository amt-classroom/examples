package ch.heigvd.amt.inversion;

/** L'injecteur : il choisit l'implémentation et la remet au service. */
class Main {
    public static void main(String[] args) {
        var service = new ReservationService(new SmtpMailer());
        service.reserve("ada@example.com", 2);
    }
}
