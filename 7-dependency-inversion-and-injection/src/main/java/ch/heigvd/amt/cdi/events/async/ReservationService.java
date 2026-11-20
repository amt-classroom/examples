package ch.heigvd.amt.cdi.events.async;

import java.util.concurrent.CompletionStage;

import ch.heigvd.amt.festival.Reservation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/** fireAsync rend la main tout de suite : le conteneur appelle les observers sur un autre thread. */
@ApplicationScoped
public class ReservationService {
    @Inject Event<Reservation> reserved;

    public CompletionStage<Void> reserve(String email, int quantity) {
        return reserved.fireAsync(new Reservation(email, quantity))
                .thenAccept(reservation -> System.out.println("Observers done"));
    }
}
