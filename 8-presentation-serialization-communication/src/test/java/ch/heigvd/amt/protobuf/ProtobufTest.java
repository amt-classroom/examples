package ch.heigvd.amt.protobuf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.annotation.XmlRootElement;

/** Le même concert dans quatre formats : Protobuf n'écrit ni les noms des champs ni leurs types. */
class ProtobufTest {

    static final Concert CONCERT = Concert.newBuilder()
            .setId(1)
            .setArtist("Zaho de Sagazan")
            .setStage("Grande scène")
            .setStartsAt("2027-07-09T21:00:00")
            .build();

    @Test
    void aMessageIsWrittenAndReadBack() throws Exception {
        byte[] bytes = CONCERT.toByteArray();

        assertEquals(CONCERT, Concert.parseFrom(bytes));
        assertEquals(55, bytes.length);
    }

    @Test
    void theSameConcertInFourFormats() throws Exception {
        var concert = new PlainConcert(1, "Zaho de Sagazan", "Grande scène", "2027-07-09T21:00:00");

        var java = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(java)) {
            out.writeObject(concert);
        }
        var xml = new StringWriter();
        JAXBContext.newInstance(XmlConcert.class).createMarshaller().marshal(
                new XmlConcert(concert), xml);
        String json = new ObjectMapper().writeValueAsString(concert);

        assertEquals(197, java.size());
        assertEquals(184, xml.toString().getBytes(StandardCharsets.UTF_8).length);
        assertEquals(92, json.getBytes(StandardCharsets.UTF_8).length);
        assertEquals(55, CONCERT.toByteArray().length);
    }

    /** Le concert pour la sérialisation Java et pour JSON. */
    public record PlainConcert(long id, String artist, String stage, String startsAt)
            implements Serializable {}

    /** Le concert pour JAXB, qui demande une classe avec un constructeur sans argument. */
    @XmlRootElement(name = "concert")
    public static class XmlConcert {
        public long id;
        public String artist;
        public String stage;
        public String startsAt;

        public XmlConcert() {}

        XmlConcert(PlainConcert concert) {
            id = concert.id();
            artist = concert.artist();
            stage = concert.stage();
            startsAt = concert.startsAt();
        }
    }
}
