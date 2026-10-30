package ch.heigvd.amt.describe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Compile une classe {@code @Describe} avec le processor et lit ce que javac affiche. */
class DescriberProcessorTest {

    @TempDir
    Path dir;

    @Test
    void aClassWithoutPrivateFieldsCompiles() throws IOException {
        assertEquals("", compile("String host; @Hidden String token;"));
    }

    @Test
    void aPrivateFieldIsRefusedOnItsLine() throws IOException {
        assertTrue(compile("private String host;")
            .contains("Config.java:5: error: a @Describe class cannot have private fields"));
    }

    /** Compile Config avec le champ donné en ligne 5 ; rend la sortie d'erreur de javac. */
    private String compile(String field) throws IOException {
        var source = dir.resolve("Config.java");
        Files.writeString(source, """
            package ch.heigvd.amt.describe;

            @Describe
            public class Config {
                %s
            }
            """.formatted(field));
        var errors = new ByteArrayOutputStream();
        ToolProvider.getSystemJavaCompiler().run(null, null, errors,
            "--release", "21", // la version du projet, que le processor déclare supporter
            "-processor", DescriberProcessor.class.getName(),
            "-classpath", System.getProperty("java.class.path"),
            "-d", dir.toString(),
            source.toString());
        return errors.toString();
    }
}
