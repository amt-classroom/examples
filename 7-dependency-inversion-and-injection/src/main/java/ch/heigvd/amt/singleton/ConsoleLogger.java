package ch.heigvd.amt.singleton;

/**
 * Un singleton écrit à la main : la classe garde elle-même son unique instance, et ses clients
 * l'obtiennent par un appel statique. Aucun test ne peut la remplacer.
 */
public class ConsoleLogger {
    private static final ConsoleLogger
            INSTANCE = new ConsoleLogger();

    private ConsoleLogger() {}

    public static ConsoleLogger getInstance() {
        return INSTANCE;
    }

    public void log(String message) {
        System.out.println(message);
    }
}
