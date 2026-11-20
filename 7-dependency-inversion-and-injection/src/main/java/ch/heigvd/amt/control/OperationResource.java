package ch.heigvd.amt.control;

import ch.heigvd.amt.injection.container.Application;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * L'inversion de contrôle : aucun main, aucun new. Le conteneur crée la ressource, lui injecte
 * l'application et appelle perform à chaque requête HTTP.
 */
@Path("/operations")
public class OperationResource {
    @Inject Application app;

    @POST
    public void perform() {
        app.performOperation();
    }
}
