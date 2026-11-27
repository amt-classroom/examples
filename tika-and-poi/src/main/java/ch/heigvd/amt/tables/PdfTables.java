package ch.heigvd.amt.tables;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Lit les tableaux d'un PDF avec Tika. Tika reconnaît le format, choisit le parseur, et décrit
 * le document comme du XHTML, un événement SAX par balise : {@code h2}, {@code table},
 * {@code tr}, {@code td}. Le même handler lirait donc un document Word.
 */
public class PdfTables extends DefaultHandler {
    private final List<Table> tables = new ArrayList<>();
    private String title;
    private List<String> row;
    private StringBuilder text;

    public static List<Table> read(InputStream pdf) throws Exception {
        var handler = new PdfTables();
        new AutoDetectParser().parse(TikaInputStream.get(pdf), handler, new Metadata(), new ParseContext());
        return handler.tables;
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes attributes) {
        switch (name) {
            case "h2", "th", "td" -> text = new StringBuilder();
            case "tr" -> row = new ArrayList<>();
            // Un tableau sans nouveau titre est la suite du précédent, sur la page suivante.
            case "table" -> {
                if (title != null) {
                    tables.add(new Table(title, new ArrayList<>()));
                    title = null;
                }
            }
            default -> {}
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) {
        if (text != null) {
            text.append(chars, start, length);
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        switch (name) {
            case "h2" -> title = text.toString().strip();
            case "th", "td" -> row.add(text.toString().strip());
            case "tr" -> {
                var rows = tables.getLast().rows();
                // Le PDF répète les en-têtes en haut de chaque page.
                if (rows.isEmpty() || !row.equals(rows.getFirst())) {
                    rows.add(row);
                }
            }
            default -> {}
        }
        text = null;
    }
}
