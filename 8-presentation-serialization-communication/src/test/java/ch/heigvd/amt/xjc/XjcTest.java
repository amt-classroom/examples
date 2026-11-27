package ch.heigvd.amt.xjc;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.xjc.generated.Programme;
import jakarta.xml.bind.JAXBContext;

/** Les classes générées depuis le schéma écrivent et relisent le même document. */
class XjcTest {

    static final List<String> XML = List.of(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>",
            "<programme year=\"2027\">",
            "    <concert id=\"1\">",
            "        <artist>Zaho de Sagazan</artist>",
            "        <stage>Grande scène</stage>",
            "    </concert>",
            "</programme>");

    @Test
    void theGeneratedClassesWriteTheProgramme() {
        assertEquals(XML, printed(() -> {
            try {
                Main.main(new String[0]);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }));
    }

    @Test
    void theGeneratedClassesReadItBack() throws Exception {
        var programme = (Programme) JAXBContext.newInstance(Programme.class)
                .createUnmarshaller()
                .unmarshal(new StringReader(String.join("\n", XML)));

        assertEquals(2027, programme.getYear());
        assertEquals("Zaho de Sagazan", programme.getConcert().get(0).getArtist());
    }
}
