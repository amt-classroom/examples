package ch.heigvd.amt.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Helidon décrit l'API depuis les ressources et les DTO, et sert ce contrat sur /openapi. */
@HelidonTest
class ContractTest {

    @Inject
    WebTarget target;

    @Test
    void theContractDescribesTheRouteAndItsDto() {
        String contract = target.path("/openapi").request().get(String.class);
        System.out.println(contract);

        assertTrue(contract.contains("/concerts/{id}:"), contract);
        assertTrue(contract.contains("$ref: '#/components/schemas/ConcertDto'"), contract);
        assertTrue(contract.contains("format: date-time"), contract);
    }

    @Test
    void aFieldTheReaderDoesNotKnowIsRefused() {
        String body = """
                {"artist":"Stromae","stage":"Grande scène","startsAt":"2027-07-10T22:30:00",\
                "genre":"pop"}""";

        try (Response response = target.path("/concerts").request()
                .post(Entity.entity(body, MediaType.APPLICATION_JSON))) {
            assertEquals(400, response.getStatus());
        }
    }
}
