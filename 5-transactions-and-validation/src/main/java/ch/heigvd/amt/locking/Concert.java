package ch.heigvd.amt.locking;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/** Un concert protégé par sa version : de deux modifications simultanées, la seconde échoue. */
@Entity
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    private String artist;

    private String description;

    @Version
    private int version;

    protected Concert() {} // pour JPA

    public Concert(String artist) {
        this.artist = artist;
    }

    public Long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
