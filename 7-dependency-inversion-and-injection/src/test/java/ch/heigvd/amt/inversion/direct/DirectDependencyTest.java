package ch.heigvd.amt.inversion.direct;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Le couplage fort : impossible de tester ce service sans son SmtpMailer. */
class DirectDependencyTest {

    @Test
    void theServiceAlwaysSendsARealMail() {
        var service = new ReservationService();

        assertEquals(List.of("SMTP to ada@example.com: 2 tickets reserved"),
                printed(() -> service.reserve("ada@example.com", 2)));
    }
}
