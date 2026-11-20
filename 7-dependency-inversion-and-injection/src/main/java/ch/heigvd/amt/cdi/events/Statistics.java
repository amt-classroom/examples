package ch.heigvd.amt.cdi.events;

import ch.heigvd.amt.festival.Reservation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/** Le conteneur appelle onReserved pour chaque événement de type Reservation. */
@ApplicationScoped
public class Statistics {
    private int tickets;

    void onReserved(@Observes Reservation reservation) {
        tickets += reservation.quantity();
    }

    public int tickets() {
        return tickets;
    }
}
