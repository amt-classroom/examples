package ch.heigvd.amt.presentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.heigvd.amt.errors.StageBusyException;
import jakarta.enterprise.context.ApplicationScoped;

/** La couche métier, réduite au programme du festival gardé en mémoire. */
@ApplicationScoped
public class ConcertService {
    private final List<Concert> concerts = new ArrayList<>(List.of(
            new Concert(1L, "Zaho de Sagazan", "Grande scène",
                    LocalDateTime.of(2027, 7, 9, 21, 0), 80_000)));

    public synchronized List<Concert> list() {
        return List.copyOf(concerts);
    }

    public synchronized Optional<Concert> find(long id) {
        return concerts.stream().filter(c -> c.getId() == id).findFirst();
    }

    public synchronized Concert schedule(String artist, String stage, LocalDateTime startsAt) {
        for (Concert other : concerts) {
            if (other.getStage().equals(stage) && other.getStartsAt().equals(startsAt)) {
                throw new StageBusyException(stage, startsAt);
            }
        }
        var concert = new Concert(concerts.size() + 1L, artist, stage, startsAt, 0);
        concerts.add(concert);
        return concert;
    }
}
