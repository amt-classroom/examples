package ch.heigvd.amt.inversion.direct;

/** Le module de haut niveau crée lui-même le module de bas niveau : il en dépend directement. */
class Application {
    private final ConsoleLogger logger = new ConsoleLogger();

    void performOperation() {
        logger.log("Performing an operation");
    }
}
