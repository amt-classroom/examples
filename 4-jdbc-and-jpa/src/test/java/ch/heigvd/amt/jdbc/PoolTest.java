package ch.heigvd.amt.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.postgresql.jdbc.PgConnection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.heigvd.amt.Postgres;

/** Le pool de connexions : close() rend la connexion, et un pool trop petit peut se bloquer lui-même. */
class PoolTest {

    @Test
    void closeHandsTheConnectionBackToThePool() throws SQLException {
        try (HikariDataSource dataSource = new HikariDataSource(config(10))) {
            PgConnection lent;
            try (Connection connection = dataSource.getConnection()) {
                lent = connection.unwrap(PgConnection.class);
            } // close() rend la connexion au pool

            assertFalse(lent.isClosed()); // la connexion à PostgreSQL reste ouverte, prête pour le suivant
        }
    }

    @Test
    void theEleventhCallWaitsThenFails() throws SQLException {
        assertEquals(30_000, new HikariConfig().getConnectionTimeout()); // les 30 secondes par défaut

        HikariConfig config = config(10);
        config.setConnectionTimeout(250); // le minimum de HikariCP, pour ne pas attendre 30 secondes
        var lent = new ArrayList<Connection>();
        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            for (int i = 0; i < 10; i++) {
                lent.add(dataSource.getConnection());
            }
            assertThrows(SQLTransientConnectionException.class, dataSource::getConnection);
        } finally {
            for (var connection : lent) {
                connection.close();
            }
        }
    }

    /** Chaque thread garde sa connexion et en demande une seconde : seul un délai écoulé les débloque. */
    @Test
    void threadsThatNeedTwoConnectionsTimeOut() throws Exception {
        assertTrue(timeouts(10, 10) > 0);
    }

    /** La taille que donne le wiki de HikariCP : threads × (connexions par thread − 1) + 1. */
    @Test
    void oneSpareConnectionBreaksTheCycle() throws Exception {
        int threads = 10;
        int connectionsPerThread = 2;
        assertEquals(0, timeouts(threads, threads * (connectionsPerThread - 1) + 1));
    }

    /**
     * Lance des threads qui empruntent une connexion, puis une seconde avant de rendre la
     * première ; compte ceux qui n'ont pas obtenu la seconde à temps.
     */
    private static long timeouts(int threads, int poolSize) throws Exception {
        HikariConfig config = config(poolSize);
        config.setConnectionTimeout(1_000);
        try (var dataSource = new HikariDataSource(config);
                var executor = Executors.newFixedThreadPool(threads)) {
            var everyoneHoldsOne = new CyclicBarrier(threads);
            Callable<Boolean> borrowTwo = () -> {
                Connection first = dataSource.getConnection();
                try {
                    everyoneHoldsOne.await(); // le pire cas : chacun tient sa première connexion
                    dataSource.getConnection().close();
                    return true;
                } catch (SQLTransientConnectionException e) {
                    return false;
                } finally {
                    first.close();
                }
            };

            long timeouts = 0;
            for (var result : executor.invokeAll(Collections.nCopies(threads, borrowTwo))) {
                if (!result.get()) {
                    timeouts++;
                }
            }
            return timeouts;
        }
    }

    /** Un pool HikariCP sur la base des tests. */
    private static HikariConfig config(int maximumPoolSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Postgres.url());
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setMaximumPoolSize(maximumPoolSize);
        return config;
    }
}
