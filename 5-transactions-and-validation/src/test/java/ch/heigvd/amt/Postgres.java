package ch.heigvd.amt;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.jpa.boot.spi.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManagerFactory;

/**
 * La base PostgreSQL 18 des tests, démarrée une fois pour toute la suite dans un conteneur. Elle
 * accepte les transactions préparées. Les bases sales et accounting, celles de la transaction
 * distribuée, journalisent chaque requête.
 *
 * <p>Un test Helidon déclare {@code @ExtendWith(Postgres.class)} avant {@code @HelidonTest} :
 * le conteneur démarre avant Helidon, dont la configuration pointe alors sur lui.
 */
public final class Postgres implements BeforeAllCallback {

    private static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer("postgres:18-alpine")
        .withDatabaseName("ticketing")
        .withUsername("postgres")
        .withPassword("postgres")
        // Contrairement à l'image par défaut de Testcontainers, garder fsync : une validation
        // doit coûter une écriture sur le disque, comme en production.
        .withCommand("postgres", "-c", "fsync=on", "-c", "max_prepared_transactions=10");

    static {
        CONTAINER.start(); // arrêté par Testcontainers à la fin de la JVM
        try {
            execute("CREATE DATABASE sales");
            execute("CREATE DATABASE accounting");
            execute("sales", """
                    ALTER DATABASE sales SET log_statement = 'all';
                    CREATE TABLE sale (id SERIAL PRIMARY KEY, email TEXT NOT NULL, quantity INT NOT NULL)""");
            execute("accounting", """
                    ALTER DATABASE accounting SET log_statement = 'all';
                    CREATE TABLE revenue (id SERIAL PRIMARY KEY, amount INT NOT NULL CHECK (amount > 0))""");
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
        System.setProperty("javax.sql.DataSource.ticketing.dataSource.url", url("ticketing"));
        System.setProperty("distributed.sales.url", url("sales"));
        System.setProperty("distributed.accounting.url", url("accounting"));
    }

    private Postgres() {
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        // Le bloc static a déjà tout démarré.
    }

    /** Une source de données sur la base ticketing, sans pool : chaque connexion est neuve. */
    public static PGSimpleDataSource dataSource() {
        return dataSource("ticketing");
    }

    public static PGSimpleDataSource dataSource(String database) {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url(database));
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }

    /** Exécute ces requêtes sur la base ticketing, séparées par des points-virgules. */
    public static void execute(String sql) throws SQLException {
        execute("ticketing", sql);
    }

    public static void execute(String database, String sql) throws SQLException {
        try (Connection connection = dataSource(database).getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    /** La première colonne de la première ligne, lue sur la base ticketing. */
    public static Object queryValue(String sql) throws SQLException {
        return queryValue("ticketing", sql);
    }

    public static Object queryValue(String database, String sql) throws SQLException {
        try (Connection connection = dataSource(database).getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getObject(1);
        }
    }

    /** Les billets qu'il reste pour ce jour. */
    public static int remaining(String day) throws SQLException {
        return (Integer) queryValue("SELECT remaining FROM ticket_stock WHERE day = '" + day + "'");
    }

    /**
     * Le stock des trois jours du festival. Un test Helidon trouve la table que Hibernate a créée
     * au démarrage ; un test JDBC la crée.
     */
    public static void resetTicketStock(int friday, int saturday, int sunday) throws SQLException {
        execute("""
                CREATE TABLE IF NOT EXISTS ticket_stock (
                    day       VARCHAR(255) PRIMARY KEY,
                    remaining INT NOT NULL CHECK (remaining >= 0)
                );
                DELETE FROM ticket_stock;
                INSERT INTO ticket_stock (day, remaining)
                VALUES ('FRIDAY', %d), ('SATURDAY', %d), ('SUNDAY', %d)""".formatted(friday, saturday, sunday));
    }

    /**
     * Une unité de persistence-resource-local.xml, avec Hibernate seul, sur la base ticketing.
     * Hibernate la lit comme Persistence.createEntityManagerFactory lirait persistence.xml.
     */
    public static EntityManagerFactory entityManagerFactory(String unit, Map<String, Object> properties) {
        var all = new HashMap<String, Object>(properties);
        all.put("jakarta.persistence.nonJtaDataSource", dataSource());
        URL units = Postgres.class.getResource("/META-INF/persistence-resource-local.xml");
        return Bootstrap.getEntityManagerFactoryBuilder(units, unit, all).build();
    }

    /** Le journal de PostgreSQL depuis le démarrage du conteneur. */
    public static String logs() {
        return CONTAINER.getLogs();
    }

    private static String url(String database) {
        return "jdbc:postgresql://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(5432) + "/" + database;
    }
}
