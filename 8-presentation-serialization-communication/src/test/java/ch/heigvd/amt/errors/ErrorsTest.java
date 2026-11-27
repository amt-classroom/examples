package ch.heigvd.amt.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** L'exception du métier arrive au client en 409, avec un corps qui dit pourquoi. */
@HelidonTest
class ErrorsTest {

    @Inject
    WebTarget target;

    @Test
    void aBusyStageGets409AndAProblem() {
        String body = """
                {"artist":"Stromae","stage":"Grande scène","startsAt":"2027-07-09T21:00:00"}""";

        try (Response response = target.path("/concerts").request()
                .post(Entity.entity(body, MediaType.APPLICATION_JSON))) {
            assertEquals(409, response.getStatus());
            assertEquals("application/problem+json", response.getMediaType().toString());
            assertEquals("""
                    {"title":"Stage busy","status":409,\
                    "detail":"Grande scène is taken at 2027-07-09T21:00"}""",
                    response.readEntity(String.class));
        }
    }
}
