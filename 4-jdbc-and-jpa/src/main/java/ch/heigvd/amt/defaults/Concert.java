package ch.heigvd.amt.defaults;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** Un concert qui ne connaît pas sa scène : seule la scène déclare la relation. */
@Entity
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    private String artist;
}
