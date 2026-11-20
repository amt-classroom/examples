package ch.heigvd.amt.cdi.events.qualified;

import java.util.ArrayList;
import java.util.List;

import ch.heigvd.amt.festival.Reservation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/** L'observateur qualifié ne reçoit que ses événements ; l'autre les reçoit tous. */
@ApplicationScoped
public class Journal {
    private final List<String> entries = new ArrayList<>();

    void onCancelled(@Observes @Cancelled Reservation reservation) {
        entries.add("cancelled " + reservation.email());
    }

    void onAny(@Observes Reservation reservation) {
        entries.add("any " + reservation.email());
    }

    public List<String> entries() {
        return entries;
    }
}
