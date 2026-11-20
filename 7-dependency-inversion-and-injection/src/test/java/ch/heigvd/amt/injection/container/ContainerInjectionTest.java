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
@AddBean(SmtpMailer.class)
@AddBean(ReservationService.class)
class ContainerInjectionTest {

    @Inject
    ReservationService service;

    @Test
    void theContainerInjectsTheMailer() {
        assertEquals(List.of("SMTP to ada@example.com: 2 tickets reserved"),
                printed(() -> service.reserve("ada@example.com", 2)));
    }
}
