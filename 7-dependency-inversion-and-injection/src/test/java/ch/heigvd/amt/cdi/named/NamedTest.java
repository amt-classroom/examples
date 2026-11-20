package ch.heigvd.amt.cdi.named;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Container;
import jakarta.enterprise.inject.spi.DeploymentException;

/** @Named distingue deux beans du même type par une chaîne. */
class NamedTest {

    @Test
    void eachInjectionPointGetsTheBeanItNames() {
        try (var container = Container.start(CardGateway.class, TwintGateway.class, Checkout.class)) {
            var checkout = container.select(Checkout.class).get();

            assertTrue(checkout.card().supports("card"));
            assertTrue(checkout.twint().supports("twint"));
        }
    }

    @Test
    void aTypoCompilesAndFailsAtStartup() {
        // Le piège : aucun bean ne s'appelle « crad », et seul le conteneur le remarque.
        var e = assertThrows(DeploymentException.class,
                () -> Container.start(CardGateway.class, TwintGateway.class, Typo.class));

        assertTrue(e.getMessage().contains("Unsatisfied dependencies for type PaymentGateway"));
    }
}
