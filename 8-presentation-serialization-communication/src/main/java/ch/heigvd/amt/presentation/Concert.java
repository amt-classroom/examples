package ch.heigvd.amt.presentation;

import java.time.LocalDateTime;

/** Un concert du festival, tel que la couche métier le connaît, cachet de l'artiste compris. */
public class Concert {
    private final Long id;
    private final String artist;
    private final String stage;
    private final LocalDateTime startsAt;
    private final int fee;

    public Concert(Long id, String artist, String stage, LocalDateTime startsAt, int fee) {
        this.id = id;
        this.artist = artist;
        this.stage = stage;
        this.startsAt = startsAt;
        this.fee = fee;
    }

    public Long getId() { return id; }
    public String getArtist() { return artist; }
    public String getStage() { return stage; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public int getFee() { return fee; }
}
