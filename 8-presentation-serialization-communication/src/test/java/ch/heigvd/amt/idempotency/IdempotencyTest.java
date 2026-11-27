package ch.heigvd.amt.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/** Le client n'a pas reçu la réponse et rejoue sa demande : il ne réserve qu'une fois. */
@HelidonTest
class IdempotencyTest {

    static final ReservationRequest REQUEST = new ReservationRequest("ada@example.com", 2);

    @Inject
    WebTarget target;

    @Inject
    ReservationService reservations;

    @Test
    void theSameKeyReservesOnce() {
        assertEquals("/reservations/1", reserve("7f3c"));
        assertEquals("/reservations/1", reserve("7f3c"));
        assertEquals(1, reservations.count());

        assertEquals("/reservations/2", reserve("b21e"));
        assertEquals(2, reservations.count());
    }

    /** Envoie la demande avec cette clé et rend l'URL de la réservation. */
    private String reserve(String key) {
        try (Response response = target.path("/reservations").request()
                .header("Idempotency-Key", key)
                .post(Entity.json(REQUEST))) {
            assertEquals(201, response.getStatus());
            return response.getLocation().getPath();
        }
    }
}
