package ch.heigvd.amt.eager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Le concert de ch.heigvd.amt.jpa, réduit à sa scène, dont le mapping change ici. */
@Entity
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    private String artist;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private Stage stage;
}
