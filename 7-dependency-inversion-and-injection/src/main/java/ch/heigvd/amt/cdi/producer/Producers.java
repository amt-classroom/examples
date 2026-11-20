package ch.heigvd.amt.cdi.producer;

import java.time.Clock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/** Le conteneur appelle une méthode {@code @Produces} pour obtenir l'objet à injecter. */
@ApplicationScoped
public class Producers {
    @Produces
    Clock clock() {
        return Clock.systemUTC();
    }

    @Produces @Named("sender")
    String sender() {
        return "tickets@baindefoule.ch";
    }
}
