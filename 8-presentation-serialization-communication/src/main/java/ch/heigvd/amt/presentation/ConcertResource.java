package ch.heigvd.amt.presentation;

import java.net.URI;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Le programme du festival par HTTP : la ressource traduit, le service décide. */
@Path("/concerts")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ConcertResource {

    @Inject ConcertService concerts;

    @GET
    public List<ConcertDto> list() {
        return concerts.list().stream()
                .map(ConcertMapper::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public ConcertDto find(@PathParam("id") long id) {
        return concerts.find(id)
                .map(ConcertMapper::toDto)
                .orElseThrow(NotFoundException::new);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response schedule(@Valid ConcertRequest request) {
        var concert = concerts.schedule(
                request.artist(), request.stage(), request.startsAt());
        var location = URI.create("/concerts/" + concert.getId());
        return Response.created(location)
                .entity(ConcertMapper.toDto(concert))
                .build();
    }
}
