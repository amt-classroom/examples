package ch.heigvd.amt.cdi.events.qualified;

import ch.heigvd.amt.festival.Reservation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/** Deux émetteurs du même type d'événement : l'un sans qualificateur, l'autre avec. */
@ApplicationScoped
public class ReservationService {
    @Inject
    Event<Reservation> reserved;

    @Inject @Cancelled
    Event<Reservation> cancelled;

    public void reserve(Reservation reservation) {
        reserved.fire(reservation);
    }

    public void cancel(Reservation reservation) {
        cancelled.fire(reservation);
    }
}
