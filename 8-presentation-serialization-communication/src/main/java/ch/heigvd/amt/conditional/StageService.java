package ch.heigvd.amt.conditional;

import jakarta.enterprise.context.ApplicationScoped;

/** La couche métier, réduite à une scène gardée en mémoire. */
@ApplicationScoped
public class StageService {
    private Stage stage = new Stage(1, "Grande scène", 20_000, 1);

    public synchronized Stage find(long id) {
        return stage;
    }

    public synchronized Stage update(long id, String name, int capacity) {
        stage = new Stage(id, name, capacity, stage.version() + 1);
        return stage;
    }
}
