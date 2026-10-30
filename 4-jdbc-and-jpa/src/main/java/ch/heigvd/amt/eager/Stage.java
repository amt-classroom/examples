package ch.heigvd.amt.eager;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/** La scène de ch.heigvd.amt.jpa, dont les concerts se chargent à chaque lecture. */
@Entity
public class Stage {

    @Id @GeneratedValue
    private Long id;

    private String name;

    // Sur le mapping : pour tous les chargements
    @OneToMany(mappedBy = "stage", fetch = FetchType.EAGER)
    private List<Concert> concerts;

    protected Stage() {} // pour le fournisseur

    public Stage(String name) {
        this.name = name;
    }
}
