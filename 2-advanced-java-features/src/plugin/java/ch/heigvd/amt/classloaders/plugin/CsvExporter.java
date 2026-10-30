package ch.heigvd.amt.classloaders.plugin;

import ch.heigvd.amt.classloaders.Exporter;

/** Un plugin : compilé à part, jamais sur le classpath de l'application, chargé par son JAR. */
public class CsvExporter implements Exporter {

    @Override
    public String name() {
        return "csv";
    }
}
