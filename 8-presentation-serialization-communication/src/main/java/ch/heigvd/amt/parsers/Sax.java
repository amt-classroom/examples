package ch.heigvd.amt.parsers;

import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/** SAX : le parseur lit le document en flux et appelle nos méthodes à chaque événement. */
public class Sax {
    public static void printArtists(InputStream xml) throws Exception {
        var handler = new DefaultHandler() {
            final StringBuilder text = new StringBuilder();

            @Override
            public void startElement(String uri, String localName,
                                     String name, Attributes attributes) {
                text.setLength(0);
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String name) {
                if (name.equals("artist")) {
                    System.out.println(text);
                }
            }
        };
        SAXParserFactory.newInstance().newSAXParser().parse(xml, handler);
    }
}
