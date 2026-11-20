package ch.heigvd.amt.injection.setter;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.injection.container.SmtpMailer;

/** L'injection par le setter : la dépendance arrive après la création, ou jamais. */
class SetterInjectionTest {

    @Test
    void theSetterReceivesTheDependency() {
        var service = new ReservationService();
        service.setMailer(new SmtpMailer());

        assertEquals(List.of("SMTP to ada@example.com: 2 tickets reserved"),
                printed(() -> service.reserve("ada@example.com", 2)));
    }

    @Test
    void aForgottenSetterGoesUnnoticed() {
        // Le piège : l'objet existe sans sa dépendance, et réserve sans confirmer.
        var service = new ReservationService();

        assertEquals(List.of(), printed(() -> service.reserve("ada@example.com", 2)));
    }
}
