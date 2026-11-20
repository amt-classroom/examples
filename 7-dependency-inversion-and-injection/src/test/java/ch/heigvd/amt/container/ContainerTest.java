package ch.heigvd.amt.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.cdi.alternative.FakeMailer;
import ch.heigvd.amt.cdi.ambiguity.CardGateway;
import ch.heigvd.amt.doubles.ReservationService;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/**
 * Le test avec le conteneur : @HelidonTest le démarre, et le conteneur injecte dans le test de
 * vrais beans, reliés entre eux. Le test vérifie l'assemblage, pas une classe isolée.
 */
@HelidonTest
@DisableDiscovery
@AddBean(ReservationService.class)
@AddBean(CardGateway.class)
@AddBean(FakeMailer.class)
class ContainerTest {
    @Inject
    ReservationService service;

    @Inject
    FakeMailer mailer;

    @Test
    void reserves() {
        assertTrue(service.reserve("ada@example.com", 2));

        assertEquals(List.of("ada@example.com: 2 tickets reserved"), mailer.sent());
    }
}
