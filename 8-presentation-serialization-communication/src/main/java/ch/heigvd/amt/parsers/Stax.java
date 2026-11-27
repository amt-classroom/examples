package ch.heigvd.amt.parsers;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/** StAX : notre code lit le document en flux, en demandant lui-même l'événement suivant. */
public class Stax {
    public static void printArtists(InputStream xml) throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance()
                .createXMLStreamReader(xml);
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT
                    && reader.getLocalName().equals("artist")) {
                System.out.println(reader.getElementText());
            }
        }
    }
}
