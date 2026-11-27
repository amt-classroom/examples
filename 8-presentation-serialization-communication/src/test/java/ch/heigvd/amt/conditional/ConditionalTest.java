package ch.heigvd.amt.conditional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/** Deux organisateurs modifient la même scène : le second apprend que sa version est dépassée. */
@HelidonTest
class ConditionalTest {

    @Inject
    WebTarget target;

    @Test
    void theSecondWriterGets412() {
        String read;
        try (Response response = target.path("/stages/1").request().get()) {
            read = response.getHeaderString("ETag");
        }
        assertEquals("\"1\"", read);

        try (Response first = update(read, new StageDto("Grande scène", 25_000))) {
            assertEquals(200, first.getStatus());
            assertEquals("\"2\"", first.getHeaderString("ETag"));
        }
        try (Response second = update(read, new StageDto("Scène du lac", 20_000))) {
            assertEquals(412, second.getStatus());
        }
        assertEquals(new StageDto("Grande scène", 25_000),
                target.path("/stages/1").request().get(StageDto.class));
    }

    private Response update(String etag, StageDto stage) {
        return target.path("/stages/1").request()
                .header("If-Match", etag)
                .put(Entity.json(stage));
    }
}
