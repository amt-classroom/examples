package ch.heigvd.amt.rollback;

import static ch.heigvd.amt.Postgres.migrateTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Un script inverse rend la colonne qu'un DROP a supprimée, pas ses données. */
class RollbackTest {

    @Test
    void theColumnComesBackEmpty() throws SQLException, IOException, InterruptedException {
        Postgres.emptyDatabase();
        migrateTo("3");
        Postgres.execute("INSERT INTO stage (name, capacity) VALUES ('Grande Scène', 30000), ('Détour', 800)");

        Postgres.execute("ALTER TABLE stage DROP COLUMN capacity"); // la migration
        Postgres.execute("ALTER TABLE stage ADD COLUMN capacity integer"); // le script inverse

        assertEquals("""
             id |     name     | capacity
            ----+--------------+----------
              1 | Grande Scène |
              2 | Détour       |""", Postgres.psql("SELECT * FROM stage"));
    }
}
