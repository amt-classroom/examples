package ch.heigvd.amt.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Un concert du festival. Il porte la clé étrangère de sa scène : c'est le côté propriétaire. */
@Entity
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String artist;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private Stage stage;

    protected Concert() {} // pour le fournisseur

    public Concert(String artist, LocalDateTime startsAt) {
        this(artist, startsAt, null);
    }

    public Concert(String artist, LocalDateTime startsAt, Stage stage) {
        this.artist = artist;
        this.startsAt = startsAt;
        this.stage = stage;
    }

    public Long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public Stage getStage() {
        return stage;
    }
}
