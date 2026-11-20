package ch.heigvd.amt.cdi.producer;

import java.time.Clock;

import ch.heigvd.amt.festival.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** Un bean qui reçoit deux objets produits : une horloge et l'adresse de l'expéditeur. */
@ApplicationScoped
public class SmtpMailer implements Mailer {
    @Inject Clock clock;
    @Inject @Named("sender") String sender;

    public void send(String to, String message) {
        System.out.println(clock.instant() + " SMTP from " + sender + " to " + to + ": " + message);
    }

    public Clock clock() {
        return clock;
    }

    public String sender() {
        return sender;
    }
}
