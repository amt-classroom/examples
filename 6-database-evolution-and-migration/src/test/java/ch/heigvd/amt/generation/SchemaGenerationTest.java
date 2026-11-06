package ch.heigvd.amt.generation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.heigvd.amt.Postgres;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

/**
 * Chaque mode de génération du schéma, sur une table qui contient déjà une scène, quand l'attribut
 * name de l'entité devient label. Aucun ne fait évoluer le schéma sans risque.
 */
class SchemaGenerationTest {

    @BeforeEach
    void aStageInTheDatabase() throws SQLException {
        Postgres.emptyDatabase();
        try (EntityManagerFactory factory = Postgres.startHibernate("stage-name", "create");
                EntityManager em = factory.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(new Stage("Grande Scène"));
            em.getTransaction().commit();
        }
    }

    @Test
    void dropAndCreateLosesTheData() throws SQLException {
        Postgres.startHibernate("stage-name", "drop-and-create").close();

        assertEquals(0L, Postgres.queryValue("SELECT count(*) FROM stage"));
    }

    @Test
    void updateKeepsTheOldColumnAndLeavesTheNewOneEmpty() throws SQLException {
        Postgres.startHibernate("stage-label", "update").close();

        assertEquals(List.of("id", "name", "label"), Postgres.columns("stage"));
        assertEquals("Grande Scène", Postgres.queryValue("SELECT name FROM stage"));
        assertNull(Postgres.queryValue("SELECT label FROM stage"));
    }

    /** Piège : name reste NOT NULL, et la nouvelle entité ne le remplit plus. */
    @Test
    void afterUpdateTheNewEntityCannotInsert() {
        try (EntityManagerFactory factory = Postgres.startHibernate("stage-label", "update");
                EntityManager em = factory.createEntityManager()) {
            em.getTransaction().begin();
            var e = assertThrows(PersistenceException.class,
                () -> em.persist(new ch.heigvd.amt.generation.renamed.Stage("Détour")));

            assertTrue(e.getMessage().contains("null value in column \"name\""), e.getMessage());
        }
    }

    @Test
    void validateRefusesToStartOnADifference() throws SQLException {
        var e = assertThrows(PersistenceException.class, () -> Postgres.startHibernate("stage-label", "validate"));

        assertTrue(e.getCause().getMessage().contains("Schema-validation: missing column [label] in table [Stage]"),
            e.getCause().getMessage());
        assertEquals(List.of("id", "name"), Postgres.columns("stage")); // rien n'a évolué
    }

    @Test
    void validateStartsWhenTheSchemaMatches() {
        assertDoesNotThrow(() -> Postgres.startHibernate("stage-name", "validate").close());
    }

    /** create ne touche pas à une table qui existe déjà : les lignes restent, label n'arrive pas. */
    @Test
    void createDoesNotEvolveAnExistingSchema() throws SQLException {
        Postgres.startHibernate("stage-label", "create").close();

        assertEquals(List.of("id", "name"), Postgres.columns("stage"));
        assertEquals(1L, Postgres.queryValue("SELECT count(*) FROM stage"));
    }

    @Test
    void createDropRecreatesTheSchemaThenRemovesItOnClose() throws SQLException {
        EntityManagerFactory factory = Postgres.startHibernate("stage-label", "create-drop");
        assertEquals(0L, Postgres.queryValue("SELECT count(*) FROM stage"));

        factory.close();
        assertFalse(Postgres.tableExists("stage"));
    }

    @Test
    void dropRemovesTheSchemaAtStartup() throws SQLException {
        Postgres.startHibernate("stage-name", "drop").close();

        assertFalse(Postgres.tableExists("stage"));
    }

    /** Hibernate peut aussi préparer le script sans le lancer. L'équipe le relit et le corrige. */
    @Test
    void hibernatePreparesAScriptForTheTeamToReview(@TempDir Path dir) throws IOException {
        Path script = dir.resolve("V1__generated.sql");

        Postgres.startHibernate("stage-name", Map.of(
            "jakarta.persistence.schema-generation.scripts.action", "create",
            "jakarta.persistence.schema-generation.scripts.create-target", script.toString())).close();

        System.out.println(Files.readString(script));
        assertTrue(Files.readString(script).contains("name varchar(255) not null"), Files.readString(script));
    }
}
