package ch.heigvd.amt.application;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Une scène, telle que les migrations l'ont laissée. Hibernate vérifie la classe, pas plus. */
@Entity
public class Stage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private Integer capacity;

    protected Stage() {} // pour JPA

    public Stage(String label, Integer capacity) {
        this.label = label;
        this.capacity = capacity;
    }

    public String getLabel() {
        return label;
    }
}
