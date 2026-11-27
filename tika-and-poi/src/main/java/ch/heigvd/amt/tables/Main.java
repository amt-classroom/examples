package ch.heigvd.amt.tables;

import java.nio.file.Path;
import java.util.List;

/** Convertit le rapport PDF en classeur Excel. */
public class Main {
    public static void main(String[] args) throws Exception {
        List<Table> tables;
        try (var pdf = Main.class.getResourceAsStream("/report.pdf")) {
            tables = PdfTables.read(pdf);
        }
        ExcelWorkbook.write(tables, Path.of("target/report.xlsx"));

        for (Table table : tables) {
            System.out.println(table.title() + ": " + (table.rows().size() - 1) + " rows");
        }
    }
}
