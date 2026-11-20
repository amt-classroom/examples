package ch.heigvd.amt.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/**
 * Passer une valeur du client à une requête : concaténée, elle devient du SQL ; liée à un
 * paramètre d'une requête préparée, elle reste une valeur.
 */
class PreparedStatementTest {

    // Ce que le client envoie comme nom d'artiste
    private static final String INJECTION = "' OR '1' = '1";

    private Connection connection;

    @BeforeEach
    void createConcerts() throws SQLException {
        connection = DriverManager.getConnection(Postgres.url(), "postgres", "postgres");
        try (Statement statement = connection.createStatement()) {
            // Une table temporaire : elle disparaît avec la connexion
            statement.execute("""
                CREATE TEMPORARY TABLE concert (
                    id     BIGINT PRIMARY KEY,
                    artist VARCHAR(255) NOT NULL
                )""");
            statement.execute("""
                INSERT INTO concert (id, artist)
                VALUES (1, 'Zaho de Sagazan'), (2, 'Stephan Eicher'), (3, 'Justice')""");
        }
    }

    @AfterEach
    void closeConnection() throws SQLException {
        connection.close();
    }

    /** La valeur concaténée réécrit la requête : la condition devient toujours vraie. */
    @Test
    void aConcatenatedValueRewritesTheQuery() throws SQLException {
        String artist = INJECTION;
        var artists = new ArrayList<String>();

        String sql = "SELECT * FROM concert WHERE artist = '" + artist + "'";

        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                artists.add(resultSet.getString("artist"));
            }
        }

        assertEquals("SELECT * FROM concert WHERE artist = '' OR '1' = '1'", sql);
        assertEquals(3, artists.size()); // tous les concerts
    }

    /** Le pilote envoie le SQL et la valeur séparément : la base cherche un artiste de ce nom. */
    @Test
    void aPreparedStatementKeepsTheValueAValue() throws SQLException {
        assertEquals(List.of(), findByArtist(INJECTION)); // aucun artiste ne porte ce nom
        assertEquals(List.of("Justice"), findByArtist("Justice"));
    }

    private List<String> findByArtist(String artist) throws SQLException {
        var artists = new ArrayList<String>();

        String sql = "SELECT * FROM concert WHERE artist = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, artist); // les paramètres se comptent à partir de 1

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    artists.add(resultSet.getString("artist"));
                }
            }
        }
        return artists;
    }
}
