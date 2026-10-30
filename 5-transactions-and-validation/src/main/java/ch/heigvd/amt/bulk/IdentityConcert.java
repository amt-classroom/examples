package ch.heigvd.amt.bulk;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Le même concert, sa clé produite par la base à l'insertion : Hibernate ne peut plus grouper. */
@Entity
public class IdentityConcert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String artist;

    private LocalDateTime startsAt;

    protected IdentityConcert() {} // pour JPA

    public IdentityConcert(String artist, LocalDateTime startsAt) {
        this.artist = artist;
        this.startsAt = startsAt;
    }
}
