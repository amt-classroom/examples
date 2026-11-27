package ch.heigvd.amt.parsers;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NodeList;

/** Le même parseur DOM, qui refuse un document portant une déclaration DOCTYPE. */
public class SafeDom {
    public static void printArtists(InputStream xml) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);

        NodeList artists = factory.newDocumentBuilder()
                .parse(xml)
                .getElementsByTagName("artist");
        for (int i = 0; i < artists.getLength(); i++) {
            System.out.println(artists.item(i).getTextContent());
        }
    }
}
