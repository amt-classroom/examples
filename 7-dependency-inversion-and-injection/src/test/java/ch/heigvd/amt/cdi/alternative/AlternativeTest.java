package ch.heigvd.amt.cdi.alternative;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.festival.ReservationService;
import ch.heigvd.amt.festival.SmtpMailer;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** Deux beans de type Mailer, sans ambiguïté : l'alternative l'emporte. */
@HelidonTest
@DisableDiscovery
@AddBean(SmtpMailer.class)
@AddBean(FakeMailer.class)
@AddBean(ReservationService.class)
class AlternativeTest {

    @Inject
    ReservationService service;

    @Inject
    FakeMailer mailer;

    @Test
    void theAlternativeReplacesTheOrdinaryBean() {
        // Le service n'a pas changé : son point d'injection demande toujours un Mailer.
        assertEquals(List.of(), printed(() -> service.reserve("ada@example.com", 2)));

        assertEquals(List.of("ada@example.com: 2 tickets reserved"), mailer.sent());
    }
}
