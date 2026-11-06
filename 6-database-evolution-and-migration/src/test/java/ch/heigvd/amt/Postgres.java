package ch.heigvd.amt;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManagerFactory;

/**
 * La base PostgreSQL 18 des tests, démarrée une fois pour toute la suite dans un conteneur. Les
 * tests vident la base festival et la migrent eux-mêmes. L'application Helidon garde sa propre
 * base, car Flyway la migre à chaque démarrage.
 *
 * <p>Un test Helidon déclare {@code @ExtendWith(Postgres.class)} avant {@code @HelidonTest} :
 * le conteneur démarre avant Helidon, dont la configuration pointe alors sur lui.
 */
public final class Postgres implements BeforeAllCallback {

    private static final Path SCRIPTS = Path.of("src/main/resources/db/migration");

    private static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer("postgres:18-alpine")
        .withDatabaseName("festival")
        .withUsername("postgres")
        .withPassword("postgres")
        // Contrairement à l'image par défaut de Testcontainers, garder fsync : une validation
        // doit coûter une écriture sur le disque, comme en production.
        .withCommand("postgres", "-c", "fsync=on");

    static {
        CONTAINER.start(); // arrêté par Testcontainers à la fin de la JVM
        try {
            execute("CREATE DATABASE application");
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
        System.setProperty("javax.sql.DataSource.festival.dataSource.url", url("application"));
    }

    private Postgres() {
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        // Le bloc static a déjà tout démarré.
    }

    /** Une source de données sur la base festival, sans pool : chaque connexion est neuve. */
    public static PGSimpleDataSource dataSource() {
        return dataSource("festival");
    }

    public static PGSimpleDataSource dataSource(String database) {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url(database));
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }

    /** Rend la base festival vide, comme neuve. */
    public static void emptyDatabase() throws SQLException {
        execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public");
    }

    /** Exécute ces requêtes sur la base festival, séparées par des points-virgules. */
    public static void execute(String sql) throws SQLException {
        try (Connection connection = dataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    /** La première colonne de la première ligne, lue sur la base festival. */
    public static Object queryValue(String sql) throws SQLException {
        return queryValue("festival", sql);
    }

    public static Object queryValue(String database, String sql) throws SQLException {
        try (Connection connection = dataSource(database).getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getObject(1);
        }
    }

    /** Les noms des colonnes d'une table, dans leur ordre. */
    public static List<String> columns(String table) throws SQLException {
        var columns = new ArrayList<String>();
        try (Connection connection = dataSource().getConnection();
                ResultSet resultSet = connection.getMetaData().getColumns(null, "public", table, null)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }

    /** Dit si cette table existe. */
    public static boolean tableExists(String table) throws SQLException {
        return !columns(table).isEmpty();
    }

    /** Le résultat tel que psql l'affiche, sans le nombre de lignes. */
    public static String psql(String query) throws IOException, InterruptedException {
        var result = CONTAINER.execInContainer(StandardCharsets.UTF_8,
            "psql", "-U", "postgres", "-d", "festival", "-P", "footer=off", "-c", query);
        return result.getStdout().lines().map(String::stripTrailing).collect(Collectors.joining("\n")).stripTrailing();
    }

    /** Applique les scripts du projet, jusqu'à cette version. */
    public static MigrateResult migrateTo(String version) {
        return Flyway.configure().dataSource(dataSource()).target(version).load().migrate();
    }

    /** Copie dans ce dossier les scripts du projet jusqu'à cette version : un test peut les changer. */
    public static void copyScripts(Path dir, int lastVersion) throws IOException {
        try (Stream<Path> scripts = Files.list(SCRIPTS)) {
            for (Path script : scripts.toList()) {
                String name = script.getFileName().toString();
                if (Integer.parseInt(name.substring(1, name.indexOf("__"))) <= lastVersion) {
                    Files.copy(script, dir.resolve(name));
                }
            }
        }
    }

    /** Flyway, sur les scripts de ce dossier. */
    public static Flyway flyway(Path dir) {
        return Flyway.configure().dataSource(dataSource()).locations("filesystem:" + dir).load();
    }

    /**
     * Démarre Hibernate seul, sur la base festival, avec ce mode de génération du schéma. L'unité
     * vient de persistence-resource-local.xml.
     */
    public static EntityManagerFactory startHibernate(String unit, String action) {
        return startHibernate(unit, Map.of("jakarta.persistence.schema-generation.database.action", action));
    }

    /** Démarre Hibernate seul, sur la base festival, avec ces réglages. */
    public static EntityManagerFactory startHibernate(String unit, Map<String, Object> properties) {
        var all = new HashMap<>(properties);
        all.put("jakarta.persistence.nonJtaDataSource", dataSource());
        URL units = Postgres.class.getResource("/META-INF/persistence-resource-local.xml");
        return Bootstrap.getEntityManagerFactoryBuilder(units, unit, all).build();
    }

    private static String url(String database) {
        return "jdbc:postgresql://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(5432) + "/" + database;
    }
}
