package ch.heigvd.amt.jooq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.jooq.ExecuteListener;
import org.jooq.SQLDialect;
import org.jooq.codegen.GenerationTool;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.heigvd.amt.Postgres;

/**
 * La même requête en jOOQ et en SQL natif, avant et après le renommage d'une colonne. Le compilateur
 * voit les tables et les colonnes de jOOQ ; il ne voit qu'une chaîne de caractères en SQL natif.
 */
class StageReportTest {

    private static final String NATIVE_QUERY = """
            SELECT stage.name, COUNT(concert.id) AS total
            FROM stage
            JOIN concert ON concert.stage_id = stage.id
            GROUP BY stage.id, stage.name
            ORDER BY total DESC""";

    private static final Path SOURCE = Path.of("src/main/java/ch/heigvd/amt/jooq/StageReport.java");

    @BeforeEach
    void threeConcertsOnTwoStages() throws SQLException {
        Postgres.emptyDatabase();
        Postgres.migrateTo("3");
        Postgres.execute("""
                INSERT INTO stage (name) VALUES ('Grande Scène'), ('Détour');
                INSERT INTO concert (stage_id, artist_name, starts_at) VALUES
                    (1, 'Les Marmottes', '2027-07-16 20:00'),
                    (1, 'Nina Lac', '2027-07-16 22:00'),
                    (2, 'Les Marmottes', '2027-07-17 18:00')""");
    }

    @Test
    void theQueryCountsTheConcertsOfEachStage() throws SQLException {
        var rows = new StageReport(DSL.using(Postgres.dataSource(), SQLDialect.POSTGRES)).concertsPerStage();

        assertEquals(List.of("Grande Scène", "Détour"), rows.map(row -> row.value1()));
        assertEquals(List.of(2, 1), rows.map(row -> row.value2())); // jOOQ lit COUNT en Integer
    }

    /** Le SQL reste visible : jOOQ envoie ce que notre code a écrit, table par table. */
    @Test
    void jooqSendsTheSqlTheCodeSpellsOut() {
        var sent = new ArrayList<String>();
        var configuration = new DefaultConfiguration()
            .set(Postgres.dataSource())
            .set(SQLDialect.POSTGRES)
            .set(ExecuteListener.onExecuteStart(context -> sent.add(context.sql())));

        new StageReport(DSL.using(configuration)).concertsPerStage();

        assertEquals(List.of("""
            select "public"."stage"."name", count("public"."concert"."id") as "total" \
            from "public"."stage" \
            join "public"."concert" on "public"."concert"."stage_id" = "public"."stage"."id" \
            group by "public"."stage"."id", "public"."stage"."name" \
            order by "total" desc"""), sent);
    }

    /** jOOQ régénère ses classes : STAGE.LABEL remplace STAGE.NAME, et StageReport ne compile plus. */
    @Test
    void aRenamedColumnBreaksTheCompilation(@TempDir Path generated) throws Exception {
        Postgres.execute("ALTER TABLE stage RENAME COLUMN name TO label");

        generateClasses(generated);
        List<String> errors = compile(generated);

        errors.forEach(System.out::println);
        assertEquals(2, errors.size()); // le groupBy, puis le select
        assertEquals("""
            StageReport.java:[27,37] cannot find symbol
              symbol:   variable NAME
              location: variable STAGE of type ch.heigvd.amt.db.tables.Stage""", errors.get(0));
    }

    /** Piège : le SQL natif compile toujours, et seul PostgreSQL voit le renommage, à l'exécution. */
    @Test
    void aRenamedColumnBreaksTheNativeQueryAtRuntime() throws SQLException {
        Postgres.execute("ALTER TABLE stage RENAME COLUMN name TO label");

        var e = assertThrows(SQLException.class, () -> Postgres.queryValue(NATIVE_QUERY));

        assertTrue(e.getMessage().contains("column stage.name does not exist"), e.getMessage());
    }

    /** La génération que le build lance avant la compilation, sur la base de ce test. */
    private static void generateClasses(Path directory) throws Exception {
        var database = Postgres.dataSource();
        GenerationTool.generate(new Configuration()
            .withJdbc(new Jdbc()
                .withUrl(database.getUrl())
                .withUser("postgres")
                .withPassword("postgres"))
            .withGenerator(new Generator()
                .withDatabase(new Database()
                    .withInputSchema("public")
                    .withExcludes("flyway_schema_history"))
                .withTarget(new Target()
                    .withPackageName("ch.heigvd.amt.db")
                    .withDirectory(directory.toString()))));
    }

    /** Compile StageReport avec ces classes générées ; rend les erreurs comme Maven les affiche. */
    private static List<String> compile(Path generated) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var files = compiler.getStandardFileManager(null, null, null);
                Stream<Path> sources = Files.walk(generated)) {
            var units = new ArrayList<>(sources.filter(path -> path.toString().endsWith(".java")).toList());
            units.add(SOURCE);
            compiler.getTask(null, files, diagnostics,
                List.of("--release", "21", "-proc:none", "-d", generated.toString(),
                    "-classpath", System.getProperty("java.class.path")),
                null, files.getJavaFileObjectsFromPaths(units)).call();
        }
        return diagnostics.getDiagnostics().stream()
            .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
            .map(diagnostic -> "StageReport.java:[%d,%d] %s".formatted(diagnostic.getLineNumber(),
                diagnostic.getColumnNumber(), diagnostic.getMessage(Locale.ENGLISH)))
            .toList();
    }
}
