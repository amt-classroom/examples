package ch.heigvd.amt.generation.renamed;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * La même scène, une fois l'attribut name renommé en label. Il reste facultatif : avec
 * nullable = false, PostgreSQL refuserait la nouvelle colonne sur une table qui contient des
 * lignes, et le mode update ne ferait que journaliser cette erreur.
 */
@Entity
public class Stage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    protected Stage() {} // pour JPA

    public Stage(String label) {
        this.label = label;
    }
}
