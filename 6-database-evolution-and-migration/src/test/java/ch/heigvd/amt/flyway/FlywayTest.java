package ch.heigvd.amt.flyway;

import static ch.heigvd.amt.Postgres.migrateTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Flyway exécute dans l'ordre les scripts qu'il n'a pas encore exécutés, et note chacun. */
class FlywayTest {

    @BeforeEach
    void emptyDatabase() throws SQLException {
        Postgres.emptyDatabase();
    }

    @Test
    void flywayRunsOnlyTheScriptsItHasNotRunYet() {
        assertEquals(2, migrateTo("2").migrationsExecuted);
        assertEquals(1, migrateTo("3").migrationsExecuted);
        assertEquals(0, migrateTo("3").migrationsExecuted);
    }

    /** Les sommes de contrôle dépendent du texte exact des scripts. */
    @Test
    void theHistoryHoldsEachScriptAndItsChecksum() throws IOException, InterruptedException {
        migrateTo("3");

        assertEquals("""
             installed_rank | version |     description      |  checksum   | success
            ----------------+---------+----------------------+-------------+---------
                          1 | 1       | create stage table   |   353487712 | t
                          2 | 2       | create concert table |  1013088240 | t
                          3 | 3       | add stage capacity   | -2003115364 | t""",
            Postgres.psql("SELECT installed_rank, version, description, checksum, success FROM flyway_schema_history"));
    }
}
