package ch.heigvd.amt.ech;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

/** La déclaration d'impôt eCH-0119, lue par les classes que XJC génère depuis la norme. */
class EchTest {

    @Test
    void theGeneratedClassesReadATaxReturn() {
        assertEquals(List.of("Ada Lovelace", "84000"), printed(() -> {
            try {
                Main.main(new String[0]);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }));
    }

    /** Les nombres de la diapositive : treize schémas, 162 classes générées. */
    @Test
    void thirteenSchemasGenerateOneHundredSixtyTwoClasses() throws IOException {
        assertEquals(13, count(Path.of("src/main/resources/schema/ech"), ".xsd"));
        assertEquals(162, count(Path.of("target/generated-sources/jaxb-ech"), ".java"));
    }

    private static long count(Path directory, String extension) throws IOException {
        try (var files = Files.walk(directory)) {
            return files.filter(file -> file.toString().endsWith(extension)).count();
        }
    }
}
