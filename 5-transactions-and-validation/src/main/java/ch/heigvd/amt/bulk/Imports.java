package ch.heigvd.amt.bulk;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import jakarta.persistence.EntityManager;

/** Les manières d'insérer beaucoup de concerts, de la plus lente à la plus rapide. */
public final class Imports {

    private Imports() {
    }

    /** Un aller-retour par ligne, et une validation par ligne si la connexion est en autocommit. */
    public static void oneByOne(Connection connection, List<Concert> concerts) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO concert (artist, starts_at) VALUES (?, ?)")) {
            for (Concert concert : concerts) {
                statement.setString(1, concert.artist());
                statement.setObject(2, concert.startsAt());
                statement.executeUpdate(); // un aller-retour
            }
        }
    }

    /** Toutes les lignes dans un seul lot JDBC. */
    public static void batch(Connection connection, List<Concert> concerts) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO concert (artist, starts_at) VALUES (?, ?)")) {
            for (Concert concert : concerts) {
                statement.setString(1, concert.artist());
                statement.setObject(2, concert.startsAt());
                statement.addBatch();
            }
            statement.executeBatch(); // un aller-retour pour tout le lot
        }
    }

    /** Un fichier CSV entier, lu par PostgreSQL sur la connexion. */
    public static long copy(Connection connection, Reader csv) throws SQLException, IOException {
        CopyManager copy = connection.unwrap(PGConnection.class)
                .getCopyAPI();
        long rows = copy.copyIn(
                "COPY concert (artist, starts_at) FROM STDIN (FORMAT csv)",
                csv); // un Reader sur le fichier
        return rows;
    }

    /** Avec Hibernate : vider le contexte de persistance tous les 50 concerts. */
    public static void persistAll(EntityManager em, List<?> concerts) {
        for (int i = 0; i < concerts.size(); i++) {
            em.persist(concerts.get(i));
            if (i % 50 == 49) {
                em.flush(); // envoie les 50 INSERT
                em.clear(); // détache les 50 concerts
            }
        }
    }
}
