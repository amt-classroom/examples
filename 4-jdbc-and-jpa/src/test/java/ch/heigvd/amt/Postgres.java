package ch.heigvd.amt;

import java.io.StringWriter;
import java.util.Map;

import javax.sql.DataSource;

import org.testcontainers.postgresql.PostgreSQLContainer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * La base PostgreSQL 18 des tests, démarrée une fois pour toute la suite dans un conteneur.
 * Docker choisit son port : {@link #url()} le donne, à la place de {@code localhost:5432}.
 */
public final class Postgres {

    private static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer("postgres:18-alpine")
        .withDatabaseName("postgres")
        .withUsername("postgres")
        .withPassword("postgres");

    static {
        CONTAINER.start(); // arrêté par Testcontainers à la fin de la JVM
    }

    // Le pool auquel Hibernate emprunte ses connexions.
    private static final DataSource POOL = pool();

    private Postgres() {
    }

    public static String url() {
        return CONTAINER.getJdbcUrl();
    }

    /** Une unité de persistance de persistence.xml, sur cette base, son schéma recréé à vide. */
    public static EntityManagerFactory entityManagerFactory(String unit) {
        return Persistence.createEntityManagerFactory(unit,
            Map.of("jakarta.persistence.nonJtaDataSource", POOL));
    }

    /** Le DDL que Hibernate écrit pour cette unité sur cette base, sans l'exécuter. */
    public static String ddl(String unit) {
        var script = new StringWriter();
        Persistence.generateSchema(unit, Map.of(
            "jakarta.persistence.nonJtaDataSource", POOL, // pour choisir le dialecte de PostgreSQL 18
            "jakarta.persistence.schema-generation.database.action", "none",
            "jakarta.persistence.schema-generation.scripts.action", "create",
            "jakarta.persistence.schema-generation.scripts.create-target", script));
        return script.toString();
    }

    /** Rend ces entités persistantes dans leur propre transaction ; elles en sortent détachées. */
    public static void insert(EntityManagerFactory factory, Object... entities) {
        try (EntityManager em = factory.createEntityManager()) {
            em.getTransaction().begin();
            for (var entity : entities) {
                em.persist(entity);
            }
            em.getTransaction().commit();
        }
    }

    private static DataSource pool() {
        var config = new HikariConfig();
        config.setJdbcUrl(url());
        config.setUsername("postgres");
        config.setPassword("postgres");
        return new HikariDataSource(config);
    }
}
