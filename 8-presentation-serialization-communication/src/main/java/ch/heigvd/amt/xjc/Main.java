package ch.heigvd.amt.xjc;

import ch.heigvd.amt.xjc.generated.Programme;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

/** Écrit le programme avec les classes que XJC génère depuis le schéma. */
public class Main {
    public static void main(String[] args) throws Exception {
        var concert = new Programme.Concert();
        concert.setId(1);
        concert.setArtist("Zaho de Sagazan");
        concert.setStage("Grande scène");

        var programme = new Programme();
        programme.setYear(2027);
        programme.getConcert().add(concert);

        var marshaller = JAXBContext.newInstance(Programme.class)
                .createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(programme, System.out);
    }
}
