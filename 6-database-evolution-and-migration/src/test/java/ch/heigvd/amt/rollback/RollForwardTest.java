package ch.heigvd.amt.rollback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.heigvd.amt.Postgres;

/**
 * Pour corriger une migration fautive, l'équipe en écrit une nouvelle. Le schéma retrouve son état
 * d'avant, et l'historique reste vrai : il montre que la migration fautive a existé.
 */
class RollForwardTest {

    @TempDir
    Path scripts;

    @Test
    void aNewMigrationFindsThePreviousStateAndTheHistoryKeepsTheFault() throws IOException, SQLException {
        Postgres.emptyDatabase();
        Postgres.copyScripts(scripts, 3);
        Postgres.flyway(scripts).migrate();
        List<String> correctState = Postgres.columns("stage");

        Files.writeString(scripts.resolve("V4__drop_stage_capacity.sql"), "ALTER TABLE stage DROP COLUMN capacity;\n");
        Postgres.flyway(scripts).migrate();

        Files.writeString(scripts.resolve("V5__restore_stage_capacity.sql"),
            "ALTER TABLE stage ADD COLUMN capacity integer;\n");
        Postgres.flyway(scripts).migrate();

        assertEquals(correctState, Postgres.columns("stage"));
        assertEquals("drop stage capacity",
            Postgres.queryValue("SELECT description FROM flyway_schema_history WHERE version = '4'"));
        assertEquals("restore stage capacity",
            Postgres.queryValue("SELECT description FROM flyway_schema_history WHERE version = '5'"));
    }
}
