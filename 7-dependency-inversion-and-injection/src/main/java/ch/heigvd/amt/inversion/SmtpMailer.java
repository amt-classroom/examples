package ch.heigvd.amt.inversion;

/** Le détail dépend de l'abstraction. */
class SmtpMailer implements Mailer {
    public void send(String to, String message) {
        System.out.println("SMTP to " + to + ": " + message);
    }
}
