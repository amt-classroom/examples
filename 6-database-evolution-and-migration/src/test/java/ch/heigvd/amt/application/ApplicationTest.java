package ch.heigvd.amt.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.heigvd.amt.Postgres;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/**
 * L'application Helidon au démarrage : notre code lance Flyway, puis Hibernate vérifie que ses
 * classes correspondent au schéma migré. La base est la source de vérité.
 */
@ExtendWith(Postgres.class)
@HelidonTest
class ApplicationTest {

    @Inject
    Stages stages;

    @Test
    void flywayMigratedTheDatabaseAtStartup() throws SQLException {
        assertEquals("8", Postgres.queryValue("application", "SELECT max(version) FROM flyway_schema_history"));
    }

    @Test
    void hibernateWorksOnTheMigratedSchema() {
        stages.add(new Stage("Grande Scène", 30000));

        assertEquals(List.of("Grande Scène"), stages.findAll().stream().map(Stage::getLabel).toList());
    }
}
