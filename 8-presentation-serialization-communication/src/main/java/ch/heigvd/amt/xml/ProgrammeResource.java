package ch.heigvd.amt.xml;

import ch.heigvd.amt.presentation.ConcertService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Le programme en XML : la ressource rend un objet, JAXB l'écrit. */
@Path("/programme")
@ApplicationScoped
public class ProgrammeResource {

    @Inject ConcertService concerts;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Programme programme() {
        var entries = concerts.list().stream()
                .map(c -> new Programme.Entry(
                        c.getId(), c.getArtist(), c.getStage()))
                .toList();
        return new Programme(2027, entries);
    }
}
