package ch.heigvd.amt.presentation;

import java.time.LocalDateTime;

/** Ce que l'API publie d'un concert : pas le cachet. */
public record ConcertDto(Long id, String artist, String stage, LocalDateTime startsAt) {}
