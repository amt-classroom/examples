package ch.heigvd.amt.eager;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Output;
import ch.heigvd.amt.Postgres;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/** FetchType.EAGER charge à chaque lecture, et ne règle pas le N+1. */
class EagerTest {

    @Test
    void eagerDoesNotFixNPlusOne() throws Exception {
        try (EntityManagerFactory factory = Postgres.entityManagerFactory("eager");
                EntityManager em = factory.createEntityManager()) {
            Postgres.insert(factory, new Stage("Grande Scène"), new Stage("Le Club"), new Stage("Scène du Lac"));

            var sql = Output.capture(() -> em.createQuery("SELECT s FROM Stage s", Stage.class).getResultList());

            // Une requête par liste, même vide, avant même qu'on la touche.
            String concertsOfAStage = "Hibernate: select .* from Concert c1_0 where c1_0.stage_id=\\?";
            assertLinesMatch(List.of(
                "Hibernate: select s1_0.id,s1_0.name from Stage s1_0",
                concertsOfAStage,
                concertsOfAStage,
                concertsOfAStage), sql);
        }
    }
}
