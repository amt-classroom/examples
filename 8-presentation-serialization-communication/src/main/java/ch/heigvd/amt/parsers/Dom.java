package ch.heigvd.amt.parsers;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/** DOM : tout le document devient un arbre en mémoire, où l'on cherche ensuite. */
public class Dom {
    public static void printArtists(InputStream xml) throws Exception {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(xml);

        NodeList artists = document.getElementsByTagName("artist");
        for (int i = 0; i < artists.getLength(); i++) {
            System.out.println(artists.item(i).getTextContent());
        }
    }
}
