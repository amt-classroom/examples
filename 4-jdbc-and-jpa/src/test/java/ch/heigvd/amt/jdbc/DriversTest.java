package ch.heigvd.amt.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Une API, des pilotes : le code ne voit que java.sql, le pilote fournit les classes. */
class DriversTest {

    @Test
    void serviceLoaderFindsTheDriversOnTheClasspath() {
        var drivers = ServiceLoader.load(Driver.class).stream()
            .map(provider -> provider.type().getName())
            .toList();
        assertTrue(drivers.containsAll(List.of("org.postgresql.Driver", "org.sqlite.JDBC")), drivers::toString);
    }

    @Test
    void changingTheDriverDoesNotChangeTheCode() throws SQLException {
        try (Connection pg = DriverManager.getConnection(Postgres.url(), "postgres", "postgres");
                Connection sqlite = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            assertEquals("PgConnection", pg.getClass().getSimpleName());
            assertEquals("JDBC4Connection", sqlite.getClass().getSimpleName());

            assertEquals(2, onePlusOne(pg));
            assertEquals(2, onePlusOne(sqlite));
        }
    }

    /** Du code qui ne connaît que l'interface Connection. */
    private static int onePlusOne(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1 + 1")) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
