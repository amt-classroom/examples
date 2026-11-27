package ch.heigvd.amt.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Le test passe par HTTP : il voit le routage, le JSON et les codes de statut. */
@HelidonTest
class ConcertResourceTest {

    @Inject
    WebTarget target;

    @Test
    void aConcertIsPublishedWithoutItsFee() {
        String json = target.path("/concerts/1").request().get(String.class);
        System.out.println(json);

        assertEquals("""
                {"id":1,"artist":"Zaho de Sagazan","stage":"Grande scène",\
                "startsAt":"2027-07-09T21:00:00"}""", json);
    }

    @Test
    void anUnknownConcertGets404() {
        try (Response response = target.path("/concerts/42").request().get()) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    void aScheduledConcertGets201AndItsLocation() {
        String body = """
                {"artist":"Stromae","stage":"Grande scène","startsAt":"2027-07-10T22:30:00"}""";

        try (Response response = target.path("/concerts").request()
                .post(Entity.entity(body, MediaType.APPLICATION_JSON))) {
            assertEquals(201, response.getStatus());
            assertEquals("/concerts/2", response.getLocation().getPath());
            assertEquals("Stromae", response.readEntity(ConcertDto.class).artist());
        }
    }

    @Test
    void anInvalidRequestGets400() {
        String body = """
                {"artist":"","stage":"Grande scène","startsAt":"2027-07-10T22:30:00"}""";

        try (Response response = target.path("/concerts").request()
                .post(Entity.entity(body, MediaType.APPLICATION_JSON))) {
            assertEquals(400, response.getStatus());
        }
    }
}
