package ch.heigvd.amt.jpa;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import ch.heigvd.amt.Postgres;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Une billetterie vide pour chaque classe de test, et un EntityManager neuf pour chaque test :
 * un contexte de persistance par test, comme une application web en ouvre un par requête.
 */
abstract class TicketingTest {

    static EntityManagerFactory factory;

    // Remplis par insertProgramme(), détachés.
    static Stage grandeScene;
    static Concert grandCanal;

    EntityManager em;

    @BeforeAll
    static void createFactory() {
        factory = Postgres.entityManagerFactory("ticketing"); // recrée le schéma
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @BeforeEach
    void openEntityManager() {
        em = factory.createEntityManager();
    }

    @AfterEach
    void closeEntityManager() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback(); // sinon la connexion resterait empruntée au pool
        }
        em.close();
    }

    /** Le samedi 17 juillet 2027, jour de démonstration de la billetterie. */
    static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(2027, 7, 17, hour, minute);
    }

    /** L'entité telle que la base la contient, lue dans un contexte neuf. */
    static <T> T stored(Class<T> type, Object id) {
        try (EntityManager fresh = factory.createEntityManager()) {
            return fresh.find(type, id);
        }
    }

    /** Les trois scènes et les six concerts de la journée, insérés dans le désordre. */
    static void insertProgramme() {
        grandeScene = new Stage("Grande Scène");
        grandCanal = new Concert("The Grand Canal", at(21, 0), grandeScene);
        var club = new Stage("Le Club");
        var lake = new Stage("Scène du Lac");
        Postgres.insert(factory, grandeScene, club, lake,
            new Concert("Bain de Minuit", at(23, 0), grandeScene),
            grandCanal,
            new Concert("Menhirs", at(19, 30), club),
            new Concert("Vieux Port", at(18, 0), lake),
            new Concert("Pluie d'Acier", at(17, 0), grandeScene),
            new Concert("Thièle Noise", at(15, 30), club));
    }
}
