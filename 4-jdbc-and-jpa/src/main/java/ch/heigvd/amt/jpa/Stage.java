package ch.heigvd.amt.jpa;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;

/** Une scène : le côté inverse de la relation, sans colonne ni table à lui. */
@Entity
@NamedQuery(name = "Stage.findAll", query = "SELECT s FROM Stage s ORDER BY s.name")
@NamedQuery(name = "Stage.findByName", query = "SELECT s FROM Stage s WHERE s.name = :name")
public class Stage {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "stage")
    private List<Concert> concerts;

    protected Stage() {} // pour le fournisseur

    public Stage(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public List<Concert> getConcerts() {
        return concerts;
    }
}
