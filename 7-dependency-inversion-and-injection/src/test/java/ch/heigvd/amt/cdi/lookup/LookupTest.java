package ch.heigvd.amt.cdi.lookup;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.cdi.qualifier.CardGateway;
import ch.heigvd.amt.cdi.qualifier.TwintGateway;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** Instance choisit un bean pendant l'exécution, ici parmi deux beans qualifiés. */
@HelidonTest
@DisableDiscovery
@AddBean(CardGateway.class)
@AddBean(TwintGateway.class)
@AddBean(Checkout.class)
class LookupTest {

    @Inject
    Checkout checkout;

    @Test
    void theCheckoutFindsTheGatewayThatSupportsTheMethod() {
        assertTrue(checkout.pay("twint", "ada@example.com", 180));
    }

    @Test
    void noGatewaySupportsCash() {
        assertThrows(IllegalArgumentException.class, () -> checkout.pay("cash", "ada@example.com", 180));
    }
}
