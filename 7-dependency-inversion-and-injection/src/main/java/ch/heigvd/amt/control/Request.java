package ch.heigvd.amt.control;

/** Le corps JSON d'une demande de réservation. */
public record Request(String email, int quantity) {
}
