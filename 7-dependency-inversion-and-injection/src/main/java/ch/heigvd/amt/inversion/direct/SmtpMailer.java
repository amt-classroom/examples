package ch.heigvd.amt.inversion.direct;

/** Le module de bas niveau : un détail technique, l'envoi d'un e-mail. */
class SmtpMailer {
    void send(String to, String message) {
        System.out.println("SMTP to " + to + ": " + message);
    }
}
