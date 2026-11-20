package ch.heigvd.amt.inversion;

/** L'injecteur : il crée le service et le remet au client. */
class Main {
    public static void main(String[] args) {
        var app = new Application(new ConsoleLogger());
        app.performOperation();
    }
}
