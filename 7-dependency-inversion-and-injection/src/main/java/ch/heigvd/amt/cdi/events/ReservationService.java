package ch.heigvd.amt.cdi.events;

import ch.heigvd.amt.festival.Reservation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/** L'émetteur ne connaît pas ceux qui l'observent. */
@ApplicationScoped
public class ReservationService {
    @Inject Event<Reservation> reserved;

    public void reserve(String email, int quantity) {
        reserved.fire(new Reservation(email, quantity));
    }
}
