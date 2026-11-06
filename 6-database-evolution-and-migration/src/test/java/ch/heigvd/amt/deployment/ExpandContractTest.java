package ch.heigvd.amt.deployment;

import static ch.heigvd.amt.Postgres.migrateTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/**
 * Le renommage de name en label, découpé en changements rétrocompatibles : le script V7 étend le
 * schéma, le code bascule, le script V8 le contracte. Entre les deux, l'ancien code écrit name, le
 * nouveau écrit label, et le trigger recopie dans l'autre colonne ce que chacun écrit.
 */
class ExpandContractTest {

    @BeforeEach
    void aStageBeforeTheExpand() throws SQLException {
        Postgres.emptyDatabase();
        migrateTo("6");
        Postgres.execute("INSERT INTO stage (name) VALUES ('Grande Scène')");
    }

    @Test
    void theExpandCopiesTheExistingNames() throws SQLException {
        migrateTo("7");

        assertEquals("Grande Scène", Postgres.queryValue("SELECT label FROM stage WHERE id = 1"));
    }

    @Test
    void anInsertByTheOldCodeFillsLabel() throws SQLException {
        migrateTo("7");

        Postgres.execute("INSERT INTO stage (name) VALUES ('Détour')");

        assertEquals("Détour", Postgres.queryValue("SELECT label FROM stage WHERE id = 2"));
    }

    /** Le trigger remplit name avant le contrôle de sa contrainte NOT NULL. */
    @Test
    void anInsertByTheNewCodeFillsName() throws SQLException {
        migrateTo("7");

        Postgres.execute("INSERT INTO stage (label) VALUES ('Détour')");

        assertEquals("Détour", Postgres.queryValue("SELECT name FROM stage WHERE id = 2"));
    }

    @Test
    void anUpdateByTheOldCodeFillsLabel() throws SQLException {
        migrateTo("7");

        Postgres.execute("UPDATE stage SET name = 'Scène du Lac' WHERE id = 1");

        assertEquals("Scène du Lac", Postgres.queryValue("SELECT label FROM stage WHERE id = 1"));
    }

    @Test
    void anUpdateByTheNewCodeFillsName() throws SQLException {
        migrateTo("7");

        Postgres.execute("UPDATE stage SET label = 'Scène du Lac' WHERE id = 1");

        assertEquals("Scène du Lac", Postgres.queryValue("SELECT name FROM stage WHERE id = 1"));
    }

    @Test
    void theContractRemovesTheOldColumnAndKeepsTheData() throws SQLException {
        migrateTo("8");

        assertFalse(Postgres.columns("stage").contains("name"));
        assertEquals("Grande Scène", Postgres.queryValue("SELECT label FROM stage WHERE id = 1"));
    }

    /** Piège : le contract suppose que tout le code est passé à label. */
    @Test
    void afterTheContractTheOldCodeFails() {
        migrateTo("8");

        var e = assertThrows(SQLException.class, () -> Postgres.queryValue("SELECT name FROM stage"));

        assertTrue(e.getMessage().contains("column \"name\" does not exist"), e.getMessage());
    }
}
