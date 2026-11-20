package ch.heigvd.amt.cdi.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** Un producteur rend injectables une classe du JDK et une simple chaîne. */
@HelidonTest
@DisableDiscovery
@AddBean(Producers.class)
@AddBean(SmtpMailer.class)
class ProducerTest {

    @Inject
    SmtpMailer mailer;

    @Test
    void theContainerInjectsWhatTheProducersReturn() {
        assertEquals(Clock.systemUTC(), mailer.clock());
        assertEquals("tickets@baindefoule.ch", mailer.sender());
    }
}
