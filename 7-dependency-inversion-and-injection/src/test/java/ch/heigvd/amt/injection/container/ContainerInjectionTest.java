package ch.heigvd.amt.injection.container;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** L'injection par le conteneur : il crée les deux beans et relie l'un à l'autre. */
@HelidonTest
@DisableDiscovery
@AddBean(ConsoleLogger.class)
@AddBean(Application.class)
class ContainerInjectionTest {

    @Inject
    Application app;

    @Test
    void theContainerInjectsTheLogger() {
        assertEquals(List.of("Performing an operation"), printed(app::performOperation));
    }
}
