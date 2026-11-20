package ch.heigvd.amt.cdi.events.async;

import java.util.concurrent.atomic.AtomicInteger;

import ch.heigvd.amt.festival.Reservation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

/** Un observer asynchrone tourne sur un autre thread : son état doit le supporter. */
@ApplicationScoped
public class Statistics {
    private final AtomicInteger tickets = new AtomicInteger();
    private volatile String thread;

    void onReserved(@ObservesAsync Reservation reservation) {
        tickets.addAndGet(reservation.quantity());
        thread = Thread.currentThread().getName();
    }

    public int tickets() {
        return tickets.get();
    }

    /** Le thread sur lequel le conteneur a appelé l'observer. */
    public String thread() {
        return thread;
    }
}
