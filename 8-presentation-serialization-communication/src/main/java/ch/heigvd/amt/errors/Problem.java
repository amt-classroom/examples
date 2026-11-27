package ch.heigvd.amt.errors;

/** Le corps d'une réponse d'erreur, le même pour toute l'API. */
public record Problem(String title, int status, String detail) {}
