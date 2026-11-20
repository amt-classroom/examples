package ch.heigvd.amt.doubles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.cdi.ambiguity.CardGateway;

/** Le test interroge le fake comme un objet ordinaire, sans framework. */
class FakeTest {

    @Test
    void sendsTheConfirmation() {
        var mailer = new FakeMailer();
        var service = new ReservationService(new CardGateway(), mailer); // accepte tout paiement

        service.reserve("ada@example.com", 2);

        assertEquals(List.of("ada@example.com: 2 tickets reserved"), mailer.sent());
    }
}
