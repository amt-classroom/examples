package ch.heigvd.amt.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.heigvd.amt.Postgres;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/**
 * Une vente écrite dans deux bases. Narayana prépare les deux, puis les valide ; si l'une échoue,
 * il annule les deux.
 */
@ExtendWith(Postgres.class)
@HelidonTest
class DistributedTest {

    @Inject
    SaleService service;

    @BeforeEach
    void empty() throws SQLException {
        Postgres.execute("sales", "DELETE FROM sale");
        Postgres.execute("accounting", "DELETE FROM revenue");
    }

    @Test
    void bothDatabasesCommitAfterAPreparePhase() throws SQLException {
        int logStart = Postgres.logs().length();

        service.sell("jane@example.com", 2, 178);

        assertEquals(1L, Postgres.queryValue("sales", "SELECT count(*) FROM sale"));
        assertEquals(1L, Postgres.queryValue("accounting", "SELECT count(*) FROM revenue"));
        String log = Postgres.logs().substring(logStart);
        System.out.println(log);
        assertEquals(2, count(log, "PREPARE TRANSACTION"));
        assertEquals(2, count(log, "COMMIT PREPARED"));
    }

    @Test
    void aFailureInOneDatabaseRollsBackBoth() throws SQLException {
        var e = assertThrows(SQLException.class, () -> service.sell("jane@example.com", 2, 0)); // CHECK (amount > 0)
        System.out.println(e.getMessage());

        assertEquals(0L, Postgres.queryValue("sales", "SELECT count(*) FROM sale"));
        assertEquals(0L, Postgres.queryValue("accounting", "SELECT count(*) FROM revenue"));
    }

    /** Le nombre de lignes du journal qui contiennent cette requête. */
    private static long count(String log, String statement) {
        return log.lines().filter(line -> line.contains(statement)).count();
    }
}
