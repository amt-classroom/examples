package ch.heigvd.amt.cdi.lifecycle;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Container;
import ch.heigvd.amt.cdi.producer.Producers;

/** Ce que le conteneur appelle, de la création d'une instance à sa destruction. */
class LifecycleTest {

    @Test
    void theContainerConstructsInjectsInitializesThenDestroys() {
        List<String> output = printed(() -> {
            try (var container = Container.start(Producers.class, SmtpMailer.class)) {
                container.select(SmtpMailer.class).get().send("ada@example.com", "2 tickets reserved");
            }
        });

        assertEquals(List.of(
                "Created, sender = null", // dans le constructeur, rien n'est encore injecté
                "Connected as tickets@baindefoule.ch",
                "SMTP to ada@example.com: 2 tickets reserved",
                "Disconnected"), output);
    }
}
