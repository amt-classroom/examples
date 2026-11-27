package ch.heigvd.amt.idempotency;

/** Une réservation enregistrée. */
public record Reservation(long id, String email, int quantity) {}
