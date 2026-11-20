package ch.heigvd.amt.doubles;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.PaymentGateway;

/** Un stub rend des réponses choisies par le test, qui vérifie ensuite le résultat. */
class StubTest {

    @Test
    void reservesWhenThePaymentIsAccepted() {
        // Arrange
        PaymentGateway gateway = mock(PaymentGateway.class);
        Mailer mailer = mock(Mailer.class);
        when(gateway.charge("ada@example.com", 180)).thenReturn(true);
        var service = new ReservationService(gateway, mailer);

        // Act
        boolean reserved = service.reserve("ada@example.com", 2);

        // Assert
        assertTrue(reserved);
    }
}
