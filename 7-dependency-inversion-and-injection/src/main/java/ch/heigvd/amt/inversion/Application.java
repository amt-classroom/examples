package ch.heigvd.amt.inversion;

/** Le module de haut niveau ne connaît plus que l'abstraction, qu'il reçoit par son constructeur. */
class Application {
    private final Logger logger;

    Application(Logger logger) {
        this.logger = logger;
    }

    void performOperation() {
        logger.log("Performing an operation");
    }
}
