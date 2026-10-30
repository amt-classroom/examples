package ch.heigvd.amt.bulk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import ch.heigvd.amt.Postgres;

/**
 * 100 000 concerts insérés de cinq manières, de la plus lente à la plus rapide. Les durées sont
 * dans target/surefire-reports/ch.heigvd.amt.bulk.JdbcImportTest-output.txt.
 */
class JdbcImportTest {

    static final List<Concert> CONCERTS = IntStream.range(0, 100_000)
        .mapToObj(i -> new Concert("Artist " + i, LocalDateTime.of(2027, 7, 16, 14, 0).plusMinutes(i)))
        .toList();

    @BeforeEach
    void emptyTable() throws SQLException {
        Postgres.execute("""
                DROP TABLE IF EXISTS concert;
                CREATE TABLE concert (
                    id        BIGSERIAL PRIMARY KEY,
                    artist    VARCHAR(255) NOT NULL,
                    starts_at TIMESTAMP NOT NULL
                )""");
    }

    @Test
    void oneTransactionPerRow() throws SQLException {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            long start = System.nanoTime();
            Imports.oneByOne(connection, CONCERTS); // autocommit : 100 000 validations
            report("one transaction per row", start);
        }
    }

    @Test
    void oneTransaction() throws SQLException {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            long start = System.nanoTime();
            connection.setAutoCommit(false);
            Imports.oneByOne(connection, CONCERTS);
            connection.commit();
            report("one transaction, one statement per row", start);
        }
    }

    @Test
    void oneBatch() throws SQLException {
        try (Connection connection = Postgres.dataSource().getConnection()) {
            long start = System.nanoTime();
            connection.setAutoCommit(false);
            Imports.batch(connection, CONCERTS);
            connection.commit();
            report("one transaction, one batch", start);
        }
    }

    @Test
    void oneRewrittenBatch() throws SQLException {
        PGSimpleDataSource rewriting = Postgres.dataSource();
        rewriting.setReWriteBatchedInserts(true); // INSERT ... VALUES (...), (...), ...
        try (Connection connection = rewriting.getConnection()) {
            long start = System.nanoTime();
            connection.setAutoCommit(false);
            Imports.batch(connection, CONCERTS);
            connection.commit();
            report("one transaction, one batch, reWriteBatchedInserts", start);
        }
    }

    @Test
    void copy() throws SQLException, IOException {
        var csv = new StringBuilder();
        for (Concert concert : CONCERTS) {
            csv.append(concert.artist()).append(',').append(concert.startsAt()).append('\n');
        }
        try (Connection connection = Postgres.dataSource().getConnection()) {
            long start = System.nanoTime();
            Imports.copy(connection, new StringReader(csv.toString()));
            report("COPY", start);
        }
    }

    private static void report(String what, long start) throws SQLException {
        System.out.printf("%-55s %,8d ms%n", what, (System.nanoTime() - start) / 1_000_000);
        assertEquals(100_000L, Postgres.queryValue("SELECT count(*) FROM concert"));
    }
}
