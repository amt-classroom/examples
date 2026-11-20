package ch.heigvd.amt.control;

import ch.heigvd.amt.injection.container.ReservationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * L'inversion de contrôle : aucun main, aucun new. Le conteneur crée la ressource, lui injecte
 * le service et appelle reserve à chaque requête HTTP.
 */
@Path("/reservations")
public class ReservationResource {
    @Inject ReservationService service;

    @POST
    public void reserve(Request request) {
        service.reserve(
                request.email(),
                request.quantity());
    }
}
