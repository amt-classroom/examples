package ch.heigvd.amt.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/** Helidon n'a pas de session HTTP : le panier @SessionScoped n'a pas de contexte où vivre. */
@HelidonTest
class SessionTest {

    @Inject
    WebTarget target;

    @Test
    void aSessionScopedBeanFailsInHelidon() {
        try (Response response = target.path("/cart").request().post(Entity.text("FRIDAY"))) {
            assertEquals(500, response.getStatus());
        }
    }
}
