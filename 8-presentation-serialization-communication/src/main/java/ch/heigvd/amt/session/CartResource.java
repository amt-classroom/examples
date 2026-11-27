package ch.heigvd.amt.session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/** Ajoute un jour au panier de la session. */
@Path("/cart")
@ApplicationScoped
public class CartResource {
    @Inject Cart cart;

    @POST
    public void add(String day) {
        cart.add(day);
    }
}
