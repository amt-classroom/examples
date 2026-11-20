package ch.heigvd.amt.festival;

import jakarta.enterprise.context.ApplicationScoped;

/** Le fournisseur de Mailer que les exemples partagent : il écrit sur la sortie. */
@ApplicationScoped
public class SmtpMailer implements Mailer {
    public void send(String to, String message) {
        System.out.println("SMTP to " + to + ": " + message);
    }
}
