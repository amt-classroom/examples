package ch.heigvd.amt.inversion;

/** L'abstraction : ce dont le haut niveau a besoin, sans dire comment l'obtenir. */
interface Logger {
    void log(String message);
}
