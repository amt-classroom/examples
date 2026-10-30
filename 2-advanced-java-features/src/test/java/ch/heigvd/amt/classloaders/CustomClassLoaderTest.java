package ch.heigvd.amt.classloaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Deux instances de CustomClassLoader isolent la même classe : chacune a son propre Class<?>. */
class CustomClassLoaderTest {

    @TempDir
    Path dir;

    @BeforeEach
    void compileIsolated() {
        var source = dir.resolve("Isolated.java");
        try {
            Files.writeString(source, "public class Isolated {}");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        var errors = new ByteArrayOutputStream();
        ToolProvider.getSystemJavaCompiler().run(null, null, errors,
            "--release", "21", "-d", dir.toString(), source.toString());
    }

    @Test
    void twoInstancesLoadDistinctClasses() throws ClassNotFoundException {
        var parent = getClass().getClassLoader();
        var first = new CustomClassLoader(parent, dir).loadClass("Isolated");
        var second = new CustomClassLoader(parent, dir).loadClass("Isolated");

        assertEquals(first.getName(), second.getName());
        assertNotSame(first, second); // même nom, deux classes : chaque loader a la sienne
    }
}
