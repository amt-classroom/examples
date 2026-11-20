package ch.heigvd.amt.inversion;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Le couplage faible : le service accepte tout fournisseur de Mailer. */
class InversionTest {

    @Test
    void mainChoosesSmtp() {
        assertEquals(List.of("SMTP to ada@example.com: 2 tickets reserved"),
                printed(() -> Main.main(new String[0])));
    }

    @Test
    void aTestChoosesAnotherMailer() {
        List<String> sent = new ArrayList<>();
        var service = new ReservationService((to, message) -> sent.add(to + ": " + message));

        service.reserve("ada@example.com", 2);

        assertEquals(List.of("ada@example.com: 2 tickets reserved"), sent);
    }
}
