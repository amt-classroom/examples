package ch.heigvd.amt.xsd;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/** Vérifie qu'un document respecte le schéma du programme, avant de le lire. */
public class Validation {
    public static void validate(String xml) throws Exception {
        var factory = SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(
                Validation.class.getResource("/schema/programme.xsd"));
        try {
            var document = new StreamSource(new StringReader(xml));
            schema.newValidator().validate(document);
            System.out.println("Valid");
        } catch (SAXException e) {
            System.out.println("Invalid: " + e.getMessage());
        }
    }
}
