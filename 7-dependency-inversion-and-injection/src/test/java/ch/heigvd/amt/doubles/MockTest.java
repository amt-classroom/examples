package ch.heigvd.amt.doubles;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.PaymentGateway;

/** Un mock enregistre les appels qu'il reçoit, et le test vérifie les bons. */
class MockTest {

    @Test
    void sendsTheConfirmation() {
        // Arrange
        PaymentGateway gateway = mock(PaymentGateway.class);
        Mailer mailer = mock(Mailer.class);
        when(gateway.charge("ada@example.com", 180)).thenReturn(true);
        var service = new ReservationService(gateway, mailer);

        // Act
        service.reserve("ada@example.com", 2);

        // Assert
        verify(mailer).send("ada@example.com", "2 tickets reserved");
    }
}
