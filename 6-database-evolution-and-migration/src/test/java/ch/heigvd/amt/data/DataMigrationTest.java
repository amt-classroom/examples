package ch.heigvd.amt.data;

import static ch.heigvd.amt.Postgres.migrateTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.heigvd.amt.Postgres;

/** Le script V4 déplace le nom de chaque artiste vers une nouvelle table, dans une transaction. */
class DataMigrationTest {

    @BeforeEach
    void threeConcertsOfTwoArtists() throws SQLException {
        Postgres.emptyDatabase();
        migrateTo("3");
        Postgres.execute("""
                INSERT INTO stage (name) VALUES ('Grande Scène');
                INSERT INTO concert (stage_id, artist_name, starts_at) VALUES
                    (1, 'Les Marmottes', '2027-07-16 20:00'),
                    (1, 'Les Marmottes', '2027-07-17 20:00'),
                    (1, 'Nina Lac', '2027-07-16 22:00')""");
    }

    @Test
    void eachConcertKeepsItsArtist() throws SQLException {
        migrateTo("4");

        assertEquals(2L, Postgres.queryValue("SELECT count(*) FROM artist"));
        assertEquals(2L, Postgres.queryValue("""
                SELECT count(*) FROM concert
                JOIN artist ON artist.id = concert.artist_id
                WHERE artist.name = 'Les Marmottes'"""));
        assertFalse(Postgres.columns("concert").contains("artist_name"));
    }

    /** Une faute de frappe dans la dernière commande : PostgreSQL annule aussi le CREATE TABLE. */
    @Test
    void anErrorUndoesTheWholeScriptDdlIncluded(@TempDir Path scripts) throws IOException, SQLException {
        Postgres.copyScripts(scripts, 4);
        Path script = scripts.resolve("V4__create_artist_table.sql");
        Files.writeString(script,
            Files.readString(script).replace("DROP COLUMN artist_name", "DROP COLUMN artist_nam"));

        var e = assertThrows(FlywayException.class, () -> Postgres.flyway(scripts).migrate());

        assertTrue(e.getMessage().contains("column \"artist_nam\" of relation \"concert\" does not exist"),
            e.getMessage());
        assertFalse(Postgres.tableExists("artist"));
        assertFalse(Postgres.columns("concert").contains("artist_id"));
        assertEquals("3", Postgres.queryValue("SELECT max(version) FROM flyway_schema_history"));
    }
}
