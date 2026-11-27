package ch.heigvd.amt.idempotency;

/** Ce que le client envoie pour réserver des billets. */
public record ReservationRequest(String email, int quantity) {}
