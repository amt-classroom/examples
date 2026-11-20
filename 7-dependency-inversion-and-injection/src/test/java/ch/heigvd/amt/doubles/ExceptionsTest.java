package ch.heigvd.amt.doubles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.PaymentException;
import ch.heigvd.amt.festival.PaymentGateway;

/** Un stub peut lever une exception : le test atteint le chemin d'erreur sans panne. */
class ExceptionsTest {

    PaymentGateway gateway = mock(PaymentGateway.class);
    Mailer mailer = mock(Mailer.class);
    ReservationService service = new ReservationService(gateway, mailer);

    @Test
    void aFailedPaymentSendsNoMail() {
        when(gateway.charge(anyString(), anyInt()))
                .thenThrow(new PaymentException("Card declined"));

        assertThrows(PaymentException.class,
                () -> service.reserve("ada@example.com", 2));
        verify(mailer, never()).send(anyString(), anyString());
    }

    @Test
    void theFirstCallFailsThenTheNextOnesSucceed() {
        when(gateway.charge(anyString(), anyInt()))
                .thenThrow(new PaymentException("Timeout"))
                .thenReturn(true);

        assertThrows(PaymentException.class, () -> service.reserve("ada@example.com", 2));
        assertTrue(service.reserve("ada@example.com", 2));
    }
}
