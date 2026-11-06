package ch.heigvd.amt.deployment;

import static ch.heigvd.amt.Postgres.migrateTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/**
 * Pendant un déploiement progressif, l'application v1 tourne un moment sur le nouveau schéma. Un
 * changement est rétrocompatible si elle fonctionne encore.
 */
class BackwardCompatibilityTest {

    /** Ce que l'application v1 envoie à la base. */
    private static final String V1_INSERT = "INSERT INTO stage (name) VALUES ('Détour')";

    @BeforeEach
    void schemaOfTheV1Application() throws SQLException {
        Postgres.emptyDatabase();
        migrateTo("1");
    }

    @Test
    void anOptionalColumnKeepsTheV1ApplicationWorking() throws SQLException {
        Postgres.execute("ALTER TABLE stage ADD COLUMN capacity integer");

        Postgres.execute(V1_INSERT);

        assertEquals(1L, Postgres.queryValue("SELECT count(*) FROM stage"));
    }

    /** Piège : la v1 ne remplit pas une colonne qu'elle ne connaît pas. */
    @Test
    void aNotNullColumnWithoutDefaultBreaksItsInserts() throws SQLException {
        Postgres.execute("ALTER TABLE stage ADD COLUMN capacity integer NOT NULL");

        var e = assertThrows(SQLException.class, () -> Postgres.execute(V1_INSERT));

        assertTrue(e.getMessage().contains("null value in column \"capacity\""), e.getMessage());
    }

    @Test
    void aRenameBreaksIt() throws SQLException {
        Postgres.execute("ALTER TABLE stage RENAME COLUMN name TO label");

        var e = assertThrows(SQLException.class, () -> Postgres.execute(V1_INSERT));

        assertTrue(e.getMessage().contains("column \"name\" of relation \"stage\" does not exist"), e.getMessage());
    }

    /** ALTER TABLE garde son verrou jusqu'au COMMIT : même une lecture de la v1 attend. */
    @Test
    void theLocksOfAMigrationBlockItsQueries() throws SQLException {
        try (Connection migration = Postgres.dataSource().getConnection();
                Connection application = Postgres.dataSource().getConnection();
                Statement migrating = migration.createStatement();
                Statement querying = application.createStatement()) {
            migration.setAutoCommit(false);
            migrating.execute("ALTER TABLE stage ADD COLUMN capacity integer");

            querying.execute("SET lock_timeout = '200ms'");
            var e = assertThrows(SQLException.class, () -> querying.execute("SELECT name FROM stage"));

            assertEquals("55P03", e.getSQLState()); // lock_not_available
            System.out.println(e.getMessage());
        }
    }
}
