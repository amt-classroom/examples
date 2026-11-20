package ch.heigvd.amt.inversion;

/** Le détail dépend de l'abstraction. */
class ConsoleLogger implements Logger {
    public void log(String message) {
        System.out.println(message);
    }
}
