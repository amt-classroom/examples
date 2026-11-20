package ch.heigvd.amt.cdi.lifecycle;

import ch.heigvd.amt.festival.Mailer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Le conteneur construit l'instance, injecte ses dépendances, puis appelle
 * {@code @PostConstruct}. Avant de la détruire, il appelle {@code @PreDestroy}.
 */
@ApplicationScoped
public class SmtpMailer implements Mailer {
    @Inject @Named("sender") String sender;

    public SmtpMailer() {
        System.out.println("Created, sender = " + sender);
    }

    @PostConstruct
    void open() {
        System.out.println("Connected as " + sender);
    }

    @PreDestroy
    void close() {
        System.out.println("Disconnected");
    }

    public void send(String to, String message) {
        System.out.println("SMTP to " + to + ": " + message);
    }
}
