package ch.heigvd.amt.presentation;

/** Traduit un concert du métier en ce que l'API en publie. */
public class ConcertMapper {
    public static ConcertDto toDto(Concert concert) {
        return new ConcertDto(
                concert.getId(),
                concert.getArtist(),
                concert.getStage(),
                concert.getStartsAt());
    }
}
