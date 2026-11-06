package ch.heigvd.amt.application;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/** Les scènes du festival, en JPA, sur le schéma que Flyway a migré. */
@ApplicationScoped
public class Stages {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void add(Stage stage) {
        em.persist(stage);
    }

    public List<Stage> findAll() {
        return em.createQuery("SELECT s FROM Stage s ORDER BY s.id", Stage.class).getResultList();
    }
}
