package ch.heigvd.amt.presentation;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Ce que le client envoie pour programmer un concert : pas d'identifiant, le serveur le choisit. */
public record ConcertRequest(
        @NotBlank String artist,
        @NotBlank String stage,
        @NotNull LocalDateTime startsAt) {}
