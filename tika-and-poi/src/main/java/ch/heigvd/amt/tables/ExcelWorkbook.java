package ch.heigvd.amt.tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Écrit les tableaux dans un classeur Excel avec POI : une feuille par tableau. Une date ou un
 * nombre devient une cellule typée, qu'Excel sait trier et additionner, et non du texte.
 */
public class ExcelWorkbook {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void write(List<Table> tables, Path file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                var out = Files.newOutputStream(file)) {
            // Une cellule garde une valeur ; son style dit comment l'afficher.
            CellStyle date = style(workbook, "dd.mm.yyyy");
            CellStyle amount = style(workbook, "#,##0.00");

            for (Table table : tables) {
                Sheet sheet = workbook.createSheet(table.title());
                for (List<String> texts : table.rows()) {
                    Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                    for (String text : texts) {
                        Cell cell = row.createCell(row.getPhysicalNumberOfCells());
                        if (text.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                            cell.setCellValue(LocalDate.parse(text, DATE));
                            cell.setCellStyle(date);
                        } else if (text.matches("[\\d']+\\.\\d{2}")) {
                            cell.setCellValue(Double.parseDouble(text.replace("'", "")));
                            cell.setCellStyle(amount);
                        } else if (text.matches("[\\d']+")) {
                            cell.setCellValue(Integer.parseInt(text.replace("'", "")));
                        } else {
                            cell.setCellValue(text);
                        }
                    }
                }
                for (int column = 0; column < table.rows().getFirst().size(); column++) {
                    sheet.autoSizeColumn(column);
                }
            }
            workbook.write(out);
        }
    }

    private static CellStyle style(Workbook workbook, String format) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        return style;
    }
}
