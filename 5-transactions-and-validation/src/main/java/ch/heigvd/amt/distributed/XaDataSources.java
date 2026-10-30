package ch.heigvd.amt.distributed;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.postgresql.xa.PGXADataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.TransactionManager;

/** Les deux bases de la vente, chacune vue comme une source XA. */
@ApplicationScoped
public class XaDataSources {

    @Inject
    TransactionManager transactionManager;

    @Inject
    @ConfigProperty(name = "distributed.user")
    String user;

    @Inject
    @ConfigProperty(name = "distributed.password")
    String password;

    @Produces
    @Named("sales")
    @ApplicationScoped
    DataSource sales(@ConfigProperty(name = "distributed.sales.url") String url) {
        return xa(url);
    }

    @Produces
    @Named("accounting")
    @ApplicationScoped
    DataSource accounting(@ConfigProperty(name = "distributed.accounting.url") String url) {
        return xa(url);
    }

    private DataSource xa(String url) {
        var xa = new PGXADataSource();
        xa.setUrl(url);
        xa.setUser(user);
        xa.setPassword(password);
        return new EnlistingDataSource(xa, transactionManager);
    }
}
