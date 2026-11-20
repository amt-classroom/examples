package ch.heigvd.amt.doubles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.PaymentGateway;

/**
 * Les annotations de Mockito : @Mock crée une doublure, @InjectMocks crée la classe sous test
 * avec ces doublures, @Captor capture un argument.
 */
@ExtendWith(MockitoExtension.class)
class AnnotationsTest {
    @Mock PaymentGateway gateway;
    @Mock Mailer mailer;
    @InjectMocks ReservationService service;
    @Captor ArgumentCaptor<String> message;

    @Test
    void sendsTheConfirmation() {
        when(gateway.charge("ada@example.com", 180)).thenReturn(true);
        service.reserve("ada@example.com", 2);
        verify(mailer).send(eq("ada@example.com"), message.capture());
        assertEquals("2 tickets reserved", message.getValue());
    }
}
