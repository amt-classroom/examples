package ch.heigvd.amt.injection.container;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Le client. Le conteneur joue l'injecteur : il le crée et remplit le champ. Personne n'écrit new. */
@ApplicationScoped
public class Application {
    @Inject
    Logger logger;

    public void performOperation() {
        logger.log("Performing an operation");
    }
}
