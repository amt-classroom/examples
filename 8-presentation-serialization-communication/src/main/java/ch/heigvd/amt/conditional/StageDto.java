package ch.heigvd.amt.conditional;

/** Ce que l'API publie d'une scène. La version voyage dans l'en-tête ETag. */
public record StageDto(String name, int capacity) {}
