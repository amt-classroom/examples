package ch.heigvd.amt.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportTest {

    private static List<Table> tables() throws Exception {
        try (var pdf = ReportTest.class.getResourceAsStream("/report.pdf")) {
            return PdfTables.read(pdf);
        }
    }

    @Test
    void tikaFindsTheTwoTablesOfTheReport() throws Exception {
        List<Table> tables = tables();

        assertEquals(List.of("Ventes par concert", "Ventes par scène"),
                tables.stream().map(Table::title).toList());
        assertEquals(List.of("Scène", "Capacité", "Concerts", "Billets", "Recette"),
                tables.get(1).rows().getFirst());
        assertEquals(List.of("Club", "400", "14", "3'835", "176'939.80"),
                tables.get(1).rows().getLast());
    }

    @Test
    void aTableCutByAPageBreakStaysOneTable() throws Exception {
        Table concerts = tables().getFirst();

        // 42 concerts sur deux pages, et les en-têtes une seule fois.
        assertEquals(1 + 42, concerts.rows().size());
    }

    @Test
    void poiWritesTypedCells(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("report.xlsx");

        ExcelWorkbook.write(tables(), file);

        try (var workbook = new XSSFWorkbook(file.toFile())) {
            Sheet sheet = workbook.getSheet("Ventes par concert");
            Row first = sheet.getRow(1);
            assertEquals(LocalDate.of(2026, 7, 3), first.getCell(0).getLocalDateTimeCellValue().toLocalDate());
            assertEquals("Les Frères Jacquard", first.getCell(1).getStringCellValue());
            assertEquals(3217, first.getCell(3).getNumericCellValue());
            assertEquals(32.50, first.getCell(4).getNumericCellValue());
        }
    }
}
