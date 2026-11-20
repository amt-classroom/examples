package ch.heigvd.amt.cdi.decorator;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.SmtpMailer;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/** Le conteneur place le decorator devant SmtpMailer, partout où Mailer est injecté. */
@HelidonTest
@DisableDiscovery
@AddBean(value = SignatureDecorator.class, scope = Dependent.class)
@AddBean(SmtpMailer.class)
class DecoratorTest {

    @Inject
    Mailer mailer;

    @Test
    void theDecoratorSignsEveryMessage() {
        assertEquals(List.of("SMTP to ada@example.com: 2 tickets reserved -- Bain de foule"),
                printed(() -> mailer.send("ada@example.com", "2 tickets reserved")));
    }
}
