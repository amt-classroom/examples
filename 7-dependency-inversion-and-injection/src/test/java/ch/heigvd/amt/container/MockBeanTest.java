package ch.heigvd.amt.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.exceptions.misusing.NotAMockException;

import ch.heigvd.amt.cdi.alternative.FakeMailer;
import ch.heigvd.amt.doubles.ReservationService;
import ch.heigvd.amt.festival.PaymentGateway;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.AddExtension;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import io.helidon.microprofile.testing.mocking.MockBean;
import io.helidon.microprofile.testing.mocking.MockBeansCdiExtension;
import jakarta.inject.Inject;

/**
 * Le mock dans le conteneur : @MockBean remplace un bean par une doublure Mockito. Tous les
 * beans qui injectent PaymentGateway la reçoivent ; les autres beans restent réels.
 */
@HelidonTest
@DisableDiscovery
@AddExtension(MockBeansCdiExtension.class)
@AddBean(ReservationService.class)
@AddBean(FakeMailer.class)
class MockBeanTest {
    @Inject ReservationService service;
    @MockBean PaymentGateway gateway;

    @Inject
    FakeMailer mailer;

    @Test
    void reservesWhenThePaymentIsAccepted() {
        when(gateway.charge("ada@example.com", 180))
                .thenReturn(true);

        assertTrue(service.reserve("ada@example.com", 2));

        // L'effet se vérifie sur le résultat, ou sur un fake que le test interroge.
        assertEquals(List.of("ada@example.com: 2 tickets reserved"), mailer.sent());
    }

    @Test
    void theFieldIsAProxyThatVerifyRejects() {
        // Le piège : le champ reçoit un proxy du conteneur, pas la doublure elle-même.
        assertThrows(NotAMockException.class, () -> verify(gateway).charge("ada@example.com", 180));
    }
}
