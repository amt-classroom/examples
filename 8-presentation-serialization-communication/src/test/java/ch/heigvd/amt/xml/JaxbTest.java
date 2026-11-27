package ch.heigvd.amt.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

/** JAXB : marshal écrit un objet en XML, unmarshal relit le XML en objet. */
class JaxbTest {

    static final String XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <programme year="2027">
                <concert id="1">
                    <artist>Zaho de Sagazan</artist>
                    <stage>Grande scène</stage>
                </concert>
            </programme>
            """;

    @Test
    void marshalWritesTheObjectAsXml() throws Exception {
        var programme = new Programme(2027,
                List.of(new Programme.Entry(1, "Zaho de Sagazan", "Grande scène")));

        Marshaller marshaller = JAXBContext.newInstance(Programme.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        var xml = new StringWriter();
        marshaller.marshal(programme, xml);

        assertEquals(XML, xml.toString());
    }

    @Test
    void unmarshalReadsTheXmlBack() throws Exception {
        var programme = (Programme) JAXBContext.newInstance(Programme.class)
                .createUnmarshaller()
                .unmarshal(new StringReader(XML));

        assertEquals(2027, programme.year);
        assertEquals("Zaho de Sagazan", programme.concerts.get(0).artist);
    }
}
