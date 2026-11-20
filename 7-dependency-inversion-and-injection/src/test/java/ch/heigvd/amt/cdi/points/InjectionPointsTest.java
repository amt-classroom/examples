package ch.heigvd.amt.cdi.points;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.cdi.ambiguity.CardGateway;
import ch.heigvd.amt.injection.container.SmtpMailer;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** Le champ, le constructeur et la méthode : le conteneur remplit les trois. */
@HelidonTest
@DisableDiscovery
@AddBean(SmtpMailer.class)
@AddBean(CardGateway.class)
@AddBean(Statistics.class)
@AddBean(ReservationService.class)
class InjectionPointsTest {

    @Inject
    ReservationService service;

    @Test
    void theContainerFillsTheThreeInjectionPoints() {
        assertTrue(service.isComplete());
    }
}
