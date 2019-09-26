package main.java.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by alfonce on 31/07/2017.
 */
public class ExcelExportUtil {
    public static Sheet createExcelFileHeaders(Workbook workbook, List<String> columnHeaders, String sheetName) {

        //create sheet
        Sheet sheet = workbook.createSheet(sheetName);

        //create headers at row = 0;
        int cellId = 0;
        Row row = sheet.createRow(0);
        for (String header : columnHeaders) {
            Cell cell = row.createCell(cellId++);
            cell.setCellValue(header);
        }

        return sheet;
    }

    public static boolean writeExcelFile(Workbook workbook, String fileName) {
        try {
            String homeDir = System.getProperty("user.home");
            FileOutputStream outputStream = new FileOutputStream(new File(homeDir, fileName));
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
