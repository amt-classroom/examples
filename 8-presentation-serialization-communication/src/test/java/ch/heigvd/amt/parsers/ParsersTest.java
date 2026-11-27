package ch.heigvd.amt.parsers;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

/** Trois parseurs lisent le même programme et en tirent les mêmes artistes. */
class ParsersTest {

    static final String XML = """
            <programme year="2027">
                <concert id="1">
                    <artist>Zaho de Sagazan</artist>
                    <stage>Grande scène</stage>
                </concert>
                <concert id="2">
                    <artist>Stromae</artist>
                    <stage>Scène du lac</stage>
                </concert>
            </programme>
            """;

    @BeforeAll
    static void messagesInEnglish() {
        Locale.setDefault(Locale.ENGLISH); // le parseur traduit ses messages
    }

    static final List<String> ARTISTS = List.of("Zaho de Sagazan", "Stromae");

    @Test
    void domLoadsTheTreeThenSearchesIt() {
        assertEquals(ARTISTS, printed(() -> run(Dom::printArtists)));
    }

    @Test
    void saxCallsOurHandlerAsItReads() {
        assertEquals(ARTISTS, printed(() -> run(Sax::printArtists)));
    }

    @Test
    void staxLetsOurCodePullTheNextEvent() {
        assertEquals(ARTISTS, printed(() -> run(Stax::printArtists)));
    }

    /** Le piège : un document reçu nomme un fichier du serveur, et le parseur le lit. */
    @Test
    void byDefaultAnExternalEntityReadsALocalFile() throws Exception {
        String xml = documentThatNames(secretFile());

        assertEquals(List.of("TOP-SECRET"), printed(() -> run(Dom::printArtists, xml)));
        assertEquals(List.of("TOP-SECRET"), printed(() -> run(Sax::printArtists, xml)));
        assertEquals(List.of("TOP-SECRET"), printed(() -> run(Stax::printArtists, xml)));
    }

    @Test
    void aParserThatRefusesDoctypeRejectsTheDocument() throws Exception {
        var xml = new ByteArrayInputStream(
                documentThatNames(secretFile()).getBytes(StandardCharsets.UTF_8));

        var e = assertThrows(SAXParseException.class, () -> SafeDom.printArtists(xml));

        assertEquals("DOCTYPE is disallowed when the feature "
                + "\"http://apache.org/xml/features/disallow-doctype-decl\" set to true.",
                e.getMessage());
    }

    private static Path secretFile() throws IOException {
        Path secret = Files.createTempFile("secret", ".txt");
        Files.writeString(secret, "TOP-SECRET");
        return secret;
    }

    /** Un programme dont l'artiste est une entité externe : le contenu de ce fichier. */
    private static String documentThatNames(Path file) {
        return """
                <!DOCTYPE programme [<!ENTITY secret SYSTEM "%s">]>
                <programme year="2027">
                    <concert id="1">
                        <artist>&secret;</artist>
                        <stage>Grande scène</stage>
                    </concert>
                </programme>
                """.formatted(file.toUri());
    }

    interface Parser {
        void printArtists(InputStream xml) throws Exception;
    }

    private static void run(Parser parser) {
        run(parser, XML);
    }

    private static void run(Parser parser, String xml) {
        try {
            parser.printArtists(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
