package ch.heigvd.amt.validation;

import ch.heigvd.amt.transactions.ReservationService;
import ch.heigvd.amt.transactions.SoldOutException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** La réservation par HTTP. Helidon refuse une demande invalide avant d'appeler le service. */
@Path("/reservations")
@RequestScoped
public class ReservationResource {

    @Inject
    ReservationService reservations;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reserve(@Valid ReservationRequest request) {
        try {
            reservations.reserveAllOrNothing(request.days(), request.quantity());
            return Response.status(Response.Status.CREATED).build();
        } catch (SoldOutException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }
}
