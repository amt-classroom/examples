package ch.heigvd.amt.injection.setter;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.injection.container.ConsoleLogger;

/** L'injection par le setter : la dépendance arrive après la création, ou jamais. */
class SetterInjectionTest {

    @Test
    void theSetterReceivesTheDependency() {
        var app = new Application();
        app.setLogger(new ConsoleLogger());

        assertEquals(List.of("Performing an operation"), printed(app::performOperation));
    }

    @Test
    void aForgottenSetterGoesUnnoticed() {
        // Le piège : le client existe sans sa dépendance, et travaille sans rien journaliser.
        var app = new Application();

        assertEquals(List.of(), printed(app::performOperation));
    }
}
