package ch.heigvd.amt.doubles;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.PaymentGateway;

/** verify contrôle le nombre d'appels, leur ordre, et l'absence d'autres appels. */
class VerificationTest {

    @Test
    void twoAcceptedReservations() {
        PaymentGateway gateway = mock(PaymentGateway.class);
        Mailer mailer = mock(Mailer.class);
        when(gateway.charge(anyString(), anyInt())).thenReturn(true);
        var service = new ReservationService(gateway, mailer);

        service.reserve("ada@example.com", 2);
        service.reserve("bob@example.com", 1);

        verify(mailer, times(2)).send(anyString(), anyString());
        verify(mailer, never()).send(eq("eve@example.com"), anyString());
        verify(mailer, atLeastOnce()).send(anyString(), anyString());
        verify(gateway, atLeast(2)).charge(anyString(), anyInt());
        verify(mailer, atMost(2)).send(anyString(), anyString());

        InOrder inOrder = inOrder(gateway, mailer);
        inOrder.verify(gateway).charge("ada@example.com", 180);
        inOrder.verify(mailer).send("ada@example.com", "2 tickets reserved");

        verifyNoMoreInteractions(mailer);
    }
}
