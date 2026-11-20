package ch.heigvd.amt.cdi.ambiguity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Container;
import jakarta.enterprise.inject.spi.DeploymentException;

/** Chaque point d'injection doit avoir un fournisseur, et un seul : sinon rien ne démarre. */
class AmbiguityTest {

    @Test
    void twoBeansOfTheSameTypeStopTheContainer() {
        var e = assertThrows(DeploymentException.class,
                () -> Container.start(CardGateway.class, TwintGateway.class, Checkout.class));

        assertTrue(e.getMessage().contains("Ambiguous dependencies for type PaymentGateway"));
    }

    @Test
    void noBeanOfTheTypeStopsTheContainerToo() {
        var e = assertThrows(DeploymentException.class, () -> Container.start(Checkout.class));

        assertTrue(e.getMessage().contains("Unsatisfied dependencies for type PaymentGateway"));
    }
}
