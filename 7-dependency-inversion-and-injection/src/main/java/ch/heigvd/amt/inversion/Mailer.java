package ch.heigvd.amt.inversion;

/** L'abstraction : ce dont le métier a besoin, sans dire comment l'obtenir. */
interface Mailer {
    void send(String to, String message);
}
