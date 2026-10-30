package ch.heigvd.amt.bulk;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** Un concert importé en masse. Sa clé vient d'une séquence, que Hibernate réserve par 50. */
@Entity
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    private String artist;

    private LocalDateTime startsAt;

    protected Concert() {} // pour JPA

    public Concert(String artist, LocalDateTime startsAt) {
        this.artist = artist;
        this.startsAt = startsAt;
    }

    public String artist() {
        return artist;
    }

    public LocalDateTime startsAt() {
        return startsAt;
    }
}
