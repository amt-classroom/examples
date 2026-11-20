package ch.heigvd.amt.doubles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;

import ch.heigvd.amt.festival.PaymentGateway;

/** Un matcher accepte une famille de valeurs plutôt qu'une valeur exacte. */
class MatchersTest {

    PaymentGateway gateway = mock(PaymentGateway.class);

    @Test
    void theLastMatchingAnswerWins() {
        when(gateway.charge(anyString(), anyInt())).thenReturn(true);
        when(gateway.charge(eq("ada@example.com"), anyInt())).thenReturn(false);
        when(gateway.charge(anyString(), intThat(amount -> amount > 360)))
                .thenReturn(false);

        when(gateway.supports(isNull())).thenReturn(false);
        when(gateway.supports(notNull())).thenReturn(true);

        assertTrue(gateway.charge("bob@example.com", 90));
        assertFalse(gateway.charge("ada@example.com", 90));
        assertFalse(gateway.charge("bob@example.com", 450));
        assertTrue(gateway.supports("card"));
    }

    @Test
    void oneMatcherMeansAllMatchers() {
        // Le piège : une valeur exacte à côté d'un matcher. Il faut eq("ada@example.com").
        assertThrows(InvalidUseOfMatchersException.class,
                () -> when(gateway.charge("ada@example.com", anyInt())).thenReturn(true));
    }
}
