package ch.heigvd.amt.injection.container;

import ch.heigvd.amt.festival.Mailer;
import jakarta.enterprise.context.ApplicationScoped;

/** Un bean : {@code @ApplicationScoped} confie la classe au conteneur. */
@ApplicationScoped
public class SmtpMailer implements Mailer {
    public void send(String to, String message) {
        System.out.println("SMTP to " + to + ": " + message);
    }
}
