package ch.heigvd.amt.festival;

/** Ce dont la billetterie a besoin pour prévenir un acheteur, sans dire comment l'obtenir. */
public interface Mailer {
    void send(String to, String message);
}
