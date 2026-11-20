package ch.heigvd.amt.cdi.interceptor;

import jakarta.enterprise.context.ApplicationScoped;

/** Sur la classe, la liaison vaut pour toutes ses méthodes. */
@Logged
@ApplicationScoped
public class ReservationService {
    public void reserve(String email, int quantity) {
        System.out.println(quantity + " tickets reserved");
    }
}
