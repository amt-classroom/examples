package ch.heigvd.amt.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** La même application répond en XML : le format dépend de la ressource, pas du métier. */
@HelidonTest
class ProgrammeResourceTest {

    @Inject
    WebTarget target;

    @Test
    void theProgrammeIsServedAsXml() {
        String xml = target.path("/programme").request().get(String.class);
        System.out.println(xml);

        assertEquals("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>\
                <programme year="2027"><concert id="1"><artist>Zaho de Sagazan</artist>\
                <stage>Grande scène</stage></concert></programme>""", xml);
    }

    @Test
    void aClientThatOnlyAcceptsJsonGets406() {
        try (Response response = target.path("/programme")
                .request(MediaType.APPLICATION_JSON).get()) {
            assertEquals(406, response.getStatus());
        }
    }
}
