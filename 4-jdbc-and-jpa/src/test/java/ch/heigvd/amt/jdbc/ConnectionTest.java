package ch.heigvd.amt.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import ch.heigvd.amt.Output;
import ch.heigvd.amt.Postgres;

/** Ouvrir une connexion, par DriverManager ou par une source de données, puis la libérer. */
class ConnectionTest {

    @Test
    void driverManagerOpensAConnection() throws Exception {
        var output = Output.capture(() -> {
            // Le pilote est trouvé par ServiceLoader, s'il est sur le classpath
            Connection connection = DriverManager.getConnection(
                Postgres.url(), "postgres", "postgres");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT version()");

            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }

            connection.close(); // sinon le serveur garde la connexion ouverte
        });
        assertLinesMatch(List.of("PostgreSQL 18\\..*"), output);
    }

    @Test
    void aDataSourceMakesTheConnections() throws SQLException {
        // La configuration, à un seul endroit
        PGSimpleDataSource pg = new PGSimpleDataSource();
        pg.setURL(Postgres.url());
        pg.setUser("postgres");
        pg.setPassword("postgres");

        // L'usage, qui ne connaît que l'interface
        DataSource dataSource = pg;
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1));
        }
    }

    @Test
    void tryWithResourcesClosesTheConnection() throws SQLException {
        Connection opened;

        try (Connection connection = DriverManager.getConnection(Postgres.url(), "postgres", "postgres");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT version()")) {
            opened = connection;
            assertTrue(resultSet.next());
        }
        // Ici, les trois ressources sont fermées, dans l'ordre inverse de leur ouverture

        assertTrue(opened.isClosed());
    }

    /** Des connexions ouvertes sans être fermées : le serveur finit par refuser les suivantes. */
    @Test
    void connectionsLeftOpenExhaustTheServer() throws SQLException {
        var open = new ArrayList<Connection>();
        try {
            var refused = assertThrows(SQLException.class, () -> {
                while (true) {
                    open.add(DriverManager.getConnection(Postgres.url(), "postgres", "postgres"));
                }
            });
            assertEquals("FATAL: sorry, too many clients already", refused.getMessage());

            try (var statement = open.getFirst().createStatement();
                    var resultSet = statement.executeQuery("SHOW max_connections")) {
                resultSet.next();
                assertEquals("100", resultSet.getString(1)); // pour tout le serveur
            }
        } finally {
            for (var connection : open) {
                connection.close();
            }
        }
    }
}
