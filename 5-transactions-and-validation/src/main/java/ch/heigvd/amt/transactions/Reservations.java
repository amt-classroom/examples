package ch.heigvd.amt.transactions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

/** Réserver les mêmes billets pour plusieurs jours, en JDBC. */
public final class Reservations {

    private Reservations() {
    }

    /** Retire les billets d'un jour s'il en reste assez : rend 1, ou 0 si le jour est complet. */
    public static int reserve(Connection connection, String day, int quantity) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE ticket_stock SET remaining = remaining - ?
                WHERE day = ? AND remaining >= ?
                """)) {
            statement.setInt(1, quantity);
            statement.setString(2, day);
            statement.setInt(3, quantity);
            return statement.executeUpdate();
        }
    }

    /** En autocommit : chaque UPDATE est validé seul, et un jour complet n'annule rien. */
    public static void reserveWithoutTransaction(DataSource dataSource, List<String> days, int quantity)
            throws SQLException, SoldOutException {
        try (Connection connection = dataSource.getConnection()) {
            for (String day : days) {
                if (reserve(connection, day, quantity) == 0) {
                    throw new SoldOutException(day);
                }
            }
        }
    }

    /** Dans une transaction : un jour complet annule toute la réservation. */
    public static void reserveDays(DataSource dataSource, List<String> days, int quantity)
            throws SQLException, SoldOutException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (String day : days) {
                    if (reserve(connection, day, quantity) == 0) {
                        throw new SoldOutException(day);
                    }
                }
                connection.commit();
            } catch (SQLException | SoldOutException e) {
                connection.rollback(); // vendredi retrouve ses billets
                throw e;
            }
        }
    }
}
