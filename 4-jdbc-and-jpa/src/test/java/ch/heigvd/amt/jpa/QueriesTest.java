package ch.heigvd.amt.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

/** Le nombre de concerts de chaque scène, écrit de quatre manières ; puis une requête nommée. */
class QueriesTest extends TicketingTest {

    /** De la scène la plus chargée à la moins chargée. */
    private static final List<String> TOTALS = List.of("Grande Scène : 3", "Le Club : 2", "Scène du Lac : 1");

    @BeforeAll
    static void programme() {
        insertProgramme();
    }

    @Test
    @SuppressWarnings("unchecked") // createNativeQuery rend une liste brute : le résultat n'est pas typé
    void nativeSql() {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT stage.name, COUNT(concert.id) AS total
                FROM stage
                JOIN concert ON concert.stage_id = stage.id
                GROUP BY stage.id, stage.name
                ORDER BY total DESC
                """).getResultList();

        assertEquals(TOTALS, totals(rows));
    }

    @Test
    void jpql() {
        List<Object[]> rows = em.createQuery("""
                SELECT s.name, COUNT(c)
                FROM Stage s JOIN s.concerts c
                GROUP BY s.id, s.name
                ORDER BY COUNT(c) DESC
                """, Object[].class).getResultList();

        assertEquals(TOTALS, totals(rows));
    }

    @Test
    void criteria() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Stage> stage = query.from(Stage.class);
        Join<Stage, Concert> concert = stage.join("concerts");

        query.multiselect(stage.get("name"), cb.count(concert))
             .groupBy(stage.get("id"), stage.get("name"))
             .orderBy(cb.desc(cb.count(concert)));

        List<Object[]> rows = em.createQuery(query).getResultList();

        assertEquals(TOTALS, totals(rows));
    }

    @Test
    void criteriaWithTheMetamodel() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Stage> stage = query.from(Stage.class);
        Join<Stage, Concert> concert = stage.join(Stage_.concerts);

        query.multiselect(stage.get(Stage_.name), cb.count(concert))
             .groupBy(stage.get(Stage_.id), stage.get(Stage_.name))
             .orderBy(cb.desc(cb.count(concert)));

        List<Object[]> rows = em.createQuery(query).getResultList();

        assertEquals(TOTALS, totals(rows));
    }

    /** Une requête JPQL déclarée sur l'entité, appelée par son nom. */
    @Test
    void namedQuery() {
        Stage stage = em.createNamedQuery("Stage.findByName", Stage.class)
                .setParameter("name", "Grande Scène")
                .getSingleResult();

        assertEquals(grandeScene.getId(), stage.getId());
    }

    /** Une valeur concaténée devient du code : JPQL n'en protège pas plus que SQL. */
    @Test
    void aConcatenatedValueRewritesTheQuery() {
        String artist = "x' OR 'x' = 'x"; // ce qu'un formulaire de recherche pourrait recevoir

        List<Concert> concatenated = em
            .createQuery("SELECT c FROM Concert c WHERE c.artist = '" + artist + "'", Concert.class)
            .getResultList();
        List<Concert> bound = em
            .createQuery("SELECT c FROM Concert c WHERE c.artist = :artist", Concert.class)
            .setParameter("artist", artist)
            .getResultList();

        assertEquals(6, concatenated.size()); // toute la programmation
        assertEquals(0, bound.size()); // aucun artiste ne porte ce nom
    }

    /** Chaque ligne, une scène et son total : « Grande Scène : 3 ». */
    private static List<String> totals(List<Object[]> rows) {
        return rows.stream().map(row -> row[0] + " : " + row[1]).toList();
    }
}
