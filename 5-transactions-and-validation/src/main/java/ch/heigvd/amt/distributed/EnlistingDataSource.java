package ch.heigvd.amt.distributed;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * Le travail d'un pool XA, que le pool HikariCP de Helidon ne fait pas : chaque connexion
 * s'inscrit dans la transaction JTA en cours et reste ouverte jusqu'à sa fin, pour que Narayana
 * puisse la préparer, puis la valider ou l'annuler.
 */
final class EnlistingDataSource implements DataSource {

    private final XADataSource xaDataSource;
    private final TransactionManager transactionManager;

    EnlistingDataSource(XADataSource xaDataSource, TransactionManager transactionManager) {
        this.xaDataSource = xaDataSource;
        this.transactionManager = transactionManager;
    }

    @Override
    public Connection getConnection() throws SQLException {
        XAConnection xaConnection = xaDataSource.getXAConnection();
        try {
            Transaction transaction = transactionManager.getTransaction();
            transaction.enlistResource(xaConnection.getXAResource());
            transaction.registerSynchronization(new Synchronization() {
                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(int status) {
                    try {
                        xaConnection.close();
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                }
            });
        } catch (SystemException | RollbackException e) {
            throw new SQLException(e);
        }
        return xaConnection.getConnection(); // la fermer ne ferme pas la connexion XA
    }

    // Le reste de l'interface DataSource ne sert pas ici.

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> type) throws SQLException {
        throw new SQLException("Not a wrapper");
    }

    @Override
    public boolean isWrapperFor(Class<?> type) {
        return false;
    }
}
