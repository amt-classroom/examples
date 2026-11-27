package ch.heigvd.amt.ech;

import ch.ech.xmlns.ech_0119._4.Message;
import jakarta.xml.bind.JAXBContext;

/**
 * Lit une déclaration d'impôt au format eCH-0119 avec les classes que XJC génère depuis les
 * schémas de la norme : pas une ligne de parseur écrite à la main.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        try (var xml = Main.class.getResourceAsStream("/ech/declaration.xml")) {
            var message = (Message) JAXBContext.newInstance(Message.class)
                    .createUnmarshaller()
                    .unmarshal(xml);

            var form = message.getContent().getMainForm();
            var person = form.getPersonDataPartner1()
                    .getPartnerPersonIdentification();
            System.out.println(person.getFirstName()
                    + " " + person.getOfficialName());
            System.out.println(form.getRevenue()
                    .getEmployedMainRevenue().getPartner1Amount());
        }
    }
}
