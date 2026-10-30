package ch.heigvd.amt.distributed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

/** Une vente écrite dans deux bases, dans une seule transaction distribuée. */
@ApplicationScoped
public class SaleService {
    @Inject @Named("sales") DataSource sales;
    @Inject @Named("accounting") DataSource accounting;

    @Transactional(rollbackOn = SQLException.class)
    public void sell(String email, int quantity, int amount)
            throws SQLException {
        try (Connection s = sales.getConnection();
                Connection a = accounting.getConnection()) {
            update(s, "INSERT INTO sale (email, quantity) VALUES (?, ?)",
                    email, quantity);
            update(a, "INSERT INTO revenue (amount) VALUES (?)",
                    amount);
        } // au commit : PREPARE TRANSACTION dans les deux bases
    }

    private static void update(Connection connection, String sql, Object... parameters)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.executeUpdate();
        }
    }
}
