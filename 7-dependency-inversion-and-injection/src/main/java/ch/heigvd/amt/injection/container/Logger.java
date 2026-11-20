package ch.heigvd.amt.injection.container;

/** L'abstraction que le client requiert et que le service offre. */
public interface Logger {
    void log(String message);
}
