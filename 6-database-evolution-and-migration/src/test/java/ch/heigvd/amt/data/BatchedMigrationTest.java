package ch.heigvd.amt.data;

import static ch.heigvd.amt.Postgres.migrateTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.heigvd.amt.Postgres;

/** Le script V6 traite les réservations par lots. Chaque COMMIT libère les verrous du lot. */
class BatchedMigrationTest {

    @BeforeEach
    void noReservationYet() throws SQLException {
        Postgres.emptyDatabase();
        migrateTo("5");
    }

    /** Sur 2 millions de lignes, une écriture concurrente attend la fin de l'UPDATE ou d'un lot. */
    @Test
    void batchesShortenTheWaitOfAConcurrentWrite() throws Exception {
        reservations(2_000_000);
        Duration oneUpdate = waitOfAConcurrentWrite(() -> {
            Postgres.execute("UPDATE reservation SET email = lower(email)");
            return null;
        });

        Postgres.emptyDatabase();
        migrateTo("5");
        reservations(2_000_000);
        Duration batches = waitOfAConcurrentWrite(() -> migrateTo("6"));

        System.out.println("one UPDATE: the concurrent write waits " + oneUpdate.toMillis() + " ms");
        System.out.println("batches: the concurrent write waits " + batches.toMillis() + " ms");
        assertEquals("guest2000000@example.com", email(2_000_000));
        assertTrue(batches.multipliedBy(10).compareTo(oneUpdate) < 0, oneUpdate + " against " + batches);
    }

    /** Piège : par défaut, Flyway exécute le script dans une transaction, où COMMIT est interdit. */
    @Test
    void aCommitFailsInsideFlywaysTransaction(@TempDir Path scripts) throws IOException, SQLException {
        Postgres.copyScripts(scripts, 6);
        Files.delete(scripts.resolve("V6__normalize_emails.sql.conf"));
        reservations(1);

        var e = assertThrows(FlywayException.class, () -> Postgres.flyway(scripts).migrate());

        assertTrue(e.getMessage().contains("invalid transaction termination"), e.getMessage());
    }

    /** Piège : sur une table vide, la boucle ne tourne pas. Le script passe, jusqu'en production. */
    @Test
    void theSameScriptPassesOnAnEmptyTable(@TempDir Path scripts) throws IOException {
        Postgres.copyScripts(scripts, 6);
        Files.delete(scripts.resolve("V6__normalize_emails.sql.conf"));

        assertEquals(1, Postgres.flyway(scripts).migrate().migrationsExecuted);
    }

    /**
     * Hors transaction, un échec garde les premiers lots, et Flyway ne le note pas : il rejoue tout
     * le script au démarrage suivant. D'où CREATE OR REPLACE. Ici, le second lot échoue : deux
     * adresses n'en font plus qu'une, ce que l'index unique refuse.
     */
    @Test
    void aFailedRunKeepsItsFirstBatchesAndFlywayReplaysTheScript() throws SQLException {
        reservations(15_000);
        Postgres.execute("""
                CREATE UNIQUE INDEX ON reservation (email);
                INSERT INTO reservation (email, quantity) VALUES ('guest15000@example.com', 1)""");

        assertThrows(FlywayException.class, () -> migrateTo("6"));

        assertEquals("guest1@example.com", email(1));
        assertEquals("GUEST10001@EXAMPLE.COM", email(10_001));
        assertEquals("5", Postgres.queryValue("SELECT max(version) FROM flyway_schema_history"));

        Postgres.execute("DELETE FROM reservation WHERE id = 15001");

        assertEquals(1, migrateTo("6").migrationsExecuted);
        assertEquals("guest10001@example.com", email(10_001));
    }

    private static void reservations(int count) throws SQLException {
        Postgres.execute("""
                INSERT INTO reservation (email, quantity)
                SELECT 'GUEST' || i || '@EXAMPLE.COM', 1 FROM generate_series(1, %d) i""".formatted(count));
    }

    private static Object email(int id) throws SQLException {
        return Postgres.queryValue("SELECT email FROM reservation WHERE id = " + id);
    }

    /** Lance la migration, puis mesure l'attente d'une écriture sur la ligne 1. */
    private static Duration waitOfAConcurrentWrite(Callable<?> migration) throws Exception {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            long startOfMigration = System.nanoTime();
            var running = executor.submit(() -> {
                migration.call();
                Duration total = Duration.ofNanos(System.nanoTime() - startOfMigration);
                System.out.println("the migration takes " + total.toMillis() + " ms");
                return null;
            });
            Thread.sleep(1000); // la migration a déjà traité la ligne 1

            long start = System.nanoTime();
            Postgres.execute("UPDATE reservation SET quantity = 2 WHERE id = 1");
            Duration wait = Duration.ofNanos(System.nanoTime() - start);

            running.get();
            return wait;
        }
    }
}
