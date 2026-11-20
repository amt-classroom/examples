package ch.heigvd.amt.injection.container;

import jakarta.enterprise.context.ApplicationScoped;

/** Le service : {@code @ApplicationScoped} confie la classe au conteneur. */
@ApplicationScoped
public class ConsoleLogger implements Logger {
    public void log(String message) {
        System.out.println(message);
    }
}
