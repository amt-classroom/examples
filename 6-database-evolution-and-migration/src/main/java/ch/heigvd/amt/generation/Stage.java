package ch.heigvd.amt.generation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Une scène, avant le renommage de son attribut. */
@Entity
public class Stage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    protected Stage() {} // pour JPA

    public Stage(String name) {
        this.name = name;
    }
}
