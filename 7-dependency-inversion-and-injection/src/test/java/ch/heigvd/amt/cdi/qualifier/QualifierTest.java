package ch.heigvd.amt.cdi.qualifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** Un qualifier personnalisé distingue deux beans du même type par une annotation. */
@HelidonTest
@DisableDiscovery
@AddBean(CardGateway.class)
@AddBean(TwintGateway.class)
@AddBean(Checkout.class)
class QualifierTest {

    @Inject
    Checkout checkout;

    @Test
    void eachInjectionPointGetsTheBeanItQualifies() {
        assertTrue(checkout.card().supports("card"));
        assertTrue(checkout.twint().supports("twint"));
    }
}
