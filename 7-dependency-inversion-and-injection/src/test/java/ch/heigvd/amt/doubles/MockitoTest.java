package ch.heigvd.amt.doubles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.festival.PaymentGateway;

/** Mockito fabrique une doublure à partir d'une interface, sans qu'on écrive de classe. */
class MockitoTest {

    @Test
    void aDoubleAnswersWithDefaultValues() {
        PaymentGateway gateway = mock(PaymentGateway.class);

        assertFalse(gateway.charge("ada@example.com", 180));   // false
    }
}
