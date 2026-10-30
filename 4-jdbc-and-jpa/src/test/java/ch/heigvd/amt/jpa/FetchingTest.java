package ch.heigvd.amt.jpa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Output;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;

/** Quand une relation se charge, et à quel prix : le chargement paresseux, le N+1 et ses remèdes. */
class FetchingTest extends TicketingTest {

    private static final String CONCERTS_OF_A_STAGE =
        "Hibernate: select .* from Concert c1_0 where c1_0.stage_id=\\?";

    private static final String STAGES_JOINED_TO_THEIR_CONCERTS =
        "Hibernate: select .* from Stage s1_0 left join Concert c1_0 on s1_0.id=c1_0.stage_id";

    @BeforeAll
    static void programme() {
        insertProgramme();
    }

    @Test
    void aOneToManyLoadsOnFirstAccess() throws Exception {
        Stage stage = em.find(Stage.class, grandeScene.getId());

        List<Concert> concerts = stage.getConcerts();
        assertFalse(factory.getPersistenceUnitUtil().isLoaded(stage, "concerts")); // aucune requête

        var sql = Output.capture(() -> concerts.size());
        assertLinesMatch(List.of(CONCERTS_OF_A_STAGE), sql);
    }

    @Test
    void aManyToOneLoadsWithItsEntity() {
        Concert concert = em.find(Concert.class, grandCanal.getId());

        assertTrue(factory.getPersistenceUnitUtil().isLoaded(concert, "stage"));
    }

    /** Le piège : une fois le contexte fermé, plus personne ne peut charger la liste. */
    @Test
    void aDetachedEntityCannotLoadItsLazyList() {
        Stage stage;
        try (EntityManager closed = factory.createEntityManager()) {
            stage = closed.find(Stage.class, grandeScene.getId());
        }

        assertThrows(LazyInitializationException.class, () -> stage.getConcerts().size());
    }

    @Test
    void eachStageCostsOneMoreQuery() throws Exception {
        var output = Output.capture(() -> {
            List<Stage> stages = em
                .createQuery("SELECT s FROM Stage s", Stage.class)
                .getResultList();                  // 1 requête

            for (Stage stage : stages) {
                System.out.println(stage.getConcerts().size()); // + 1 par scène
            }
        });

        assertLinesMatch(List.of(
            "Hibernate: select s1_0.id,s1_0.name from Stage s1_0",
            CONCERTS_OF_A_STAGE, "\\d",
            CONCERTS_OF_A_STAGE, "\\d",
            CONCERTS_OF_A_STAGE, "\\d"), output);
    }

    @Test
    void joinFetchLoadsEverythingInOneQuery() throws Exception {
        var output = Output.capture(() -> {
            List<Stage> stages = em.createQuery("""
                    SELECT s FROM Stage s
                    LEFT JOIN FETCH s.concerts
                    """, Stage.class).getResultList(); // 1 requête

            for (Stage stage : stages) {
                System.out.println(stage.getConcerts().size());
            }
        });

        assertLinesMatch(List.of(STAGES_JOINED_TO_THEIR_CONCERTS, "\\d", "\\d", "\\d"), output);
    }

    @Test
    @SuppressWarnings("unchecked") // addAttributeNodes(Attribute<T, ?>...) : un tableau générique
    void anEntityGraphLoadsTheConcertsForOneQuery() throws Exception {
        EntityGraph<Stage> graph = em.createEntityGraph(Stage.class);
        graph.addAttributeNodes(Stage_.concerts);

        var output = Output.capture(() -> {
            // Toutes les scènes et leurs concerts : 1 requête
            List<Stage> stages = em.createQuery("SELECT s FROM Stage s", Stage.class)
                    .setHint("jakarta.persistence.fetchgraph", graph)
                    .getResultList();

            for (Stage stage : stages) {
                System.out.println(stage.getConcerts().size());
            }
        });

        assertLinesMatch(List.of(STAGES_JOINED_TO_THEIR_CONCERTS, "\\d", "\\d", "\\d"), output);
    }
}
