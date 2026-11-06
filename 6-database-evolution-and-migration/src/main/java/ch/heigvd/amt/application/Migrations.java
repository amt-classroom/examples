package ch.heigvd.amt.application;

import org.flywaydb.core.Flyway;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;
import jakarta.interceptor.Interceptor;
import javax.sql.DataSource;

/**
 * Lance Flyway au démarrage de l'application : Helidon n'a pas d'extension pour Flyway. Sans cette
 * priorité, Hibernate démarrerait d'abord et refuserait la base vide : missing table [Stage].
 */
@ApplicationScoped
public class Migrations {

    void migrate(
            @Observes
            @Priority(Interceptor.Priority.PLATFORM_BEFORE)
            @Initialized(ApplicationScoped.class) Object event,
            @Named("festival") DataSource dataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate();
    }
}
