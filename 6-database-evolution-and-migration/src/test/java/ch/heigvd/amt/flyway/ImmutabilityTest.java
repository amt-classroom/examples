package ch.heigvd.amt.flyway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.flywaydb.core.api.exception.FlywayValidateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.heigvd.amt.Postgres;

/** Une fois exécuté, un script fait partie de l'histoire de la base. Flyway le vérifie. */
class ImmutabilityTest {

    @TempDir
    Path scripts;

    @BeforeEach
    void threeScriptsAlreadyExecuted() throws IOException, SQLException {
        Postgres.emptyDatabase();
        Postgres.copyScripts(scripts, 3);
        Postgres.flyway(scripts).migrate();
    }

    @Test
    void aModifiedScriptIsRefused() throws IOException {
        Files.writeString(scripts.resolve("V3__add_stage_capacity.sql"),
            "ALTER TABLE stage ADD COLUMN capacity bigint;\n");

        var e = assertThrows(FlywayValidateException.class, () -> Postgres.flyway(scripts).migrate());

        System.out.println(e.getMessage());
        assertTrue(e.getMessage().contains("""
            Validate failed: Migrations have failed validation
            Migration checksum mismatch for migration version 3
            -> Applied to database : -2003115364
            -> Resolved locally    : -1135682106"""), e.getMessage());
    }

    @Test
    void aRenamedScriptIsRefused() throws IOException {
        Files.move(scripts.resolve("V3__add_stage_capacity.sql"), scripts.resolve("V3__add_capacity.sql"));

        var e = assertThrows(FlywayValidateException.class, () -> Postgres.flyway(scripts).migrate());

        assertTrue(e.getMessage().contains("Migration description mismatch for migration version 3"), e.getMessage());
    }

    @Test
    void aDeletedOlderScriptIsRefused() throws IOException {
        Files.delete(scripts.resolve("V2__create_concert_table.sql"));

        var e = assertThrows(FlywayValidateException.class, () -> Postgres.flyway(scripts).migrate());

        assertTrue(e.getMessage().contains("Detected applied migration not resolved locally: 2"), e.getMessage());
    }

    /** Piège : Flyway prend le dernier script pour une migration future, et laisse passer. */
    @Test
    void aDeletedLatestScriptPassesSilently() throws IOException, SQLException {
        Files.delete(scripts.resolve("V3__add_stage_capacity.sql"));

        assertEquals(0, Postgres.flyway(scripts).migrate().migrationsExecuted);
        assertEquals("3", Postgres.queryValue("SELECT max(version) FROM flyway_schema_history"));
    }
}
