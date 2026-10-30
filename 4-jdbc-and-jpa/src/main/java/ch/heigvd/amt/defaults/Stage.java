package ch.heigvd.amt.defaults;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/** Une scène qui connaît ses concerts, sans rien dire de plus à l'ORM. */
@Entity
public class Stage {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany
    private List<Concert> concerts;
}
