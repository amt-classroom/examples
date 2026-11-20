package ch.heigvd.amt.injection.setter;

import ch.heigvd.amt.injection.container.Logger;

/** Une dépendance facultative : tant que personne n'appelle le setter, le champ vaut null. */
public class Application {
    private Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void performOperation() {
        if (logger != null) {
            logger.log("Performing an operation");
        }
    }
}
