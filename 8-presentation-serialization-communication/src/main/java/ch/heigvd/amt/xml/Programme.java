package ch.heigvd.amt.xml;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/** Le programme du festival, tel que les partenaires le reçoivent en XML. */
@XmlRootElement(name = "programme")
public class Programme {

    @XmlAttribute
    public int year;

    @XmlElement(name = "concert")
    public List<Entry> concerts;

    public Programme() {} // requis par JAXB

    public Programme(int year, List<Entry> concerts) {
        this.year = year;
        this.concerts = concerts;
    }

    /** Une ligne du programme. */
    public static class Entry {
        @XmlAttribute
        public long id;
        public String artist;
        public String stage;

        public Entry() {} // requis par JAXB

        public Entry(long id, String artist, String stage) {
            this.id = id;
            this.artist = artist;
            this.stage = stage;
        }
    }
}
