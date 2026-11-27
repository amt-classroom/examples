package ch.heigvd.amt.idempotency;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** La réservation par HTTP : le client choisit une clé et la renvoie s'il rejoue sa demande. */
@Path("/reservations")
@ApplicationScoped
public class ReservationResource {
    @Inject ReservationService reservations;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reserve(@HeaderParam("Idempotency-Key") String key,
                            ReservationRequest request) {
        var reservation = reservations.reserve(
                key, request.email(), request.quantity());
        var location = URI.create("/reservations/" + reservation.id());
        return Response.created(location).build();
    }
}
