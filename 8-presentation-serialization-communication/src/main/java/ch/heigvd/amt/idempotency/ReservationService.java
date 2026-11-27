package ch.heigvd.amt.idempotency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;

/** Réserve une seule fois par clé : rejouer la demande rend la réservation déjà faite. */
@ApplicationScoped
public class ReservationService {
    private final Map<String, Reservation> byKey = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public Reservation reserve(String key, String email, int quantity) {
        return byKey.computeIfAbsent(key, k ->
                new Reservation(ids.incrementAndGet(), email, quantity));
    }

    public int count() {
        return byKey.size();
    }
}
