package com.ajustadoati.sc.application.service.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@Service
@Slf4j
public class FileService {



  private final String excelFilePath = "/Users/rojasric/projects/CAJA_DE_AHORRO.xlsx";

  public void registrarPago(String cedula, String tipoPago, double monto, String fecha) throws IOException {
    try (FileInputStream fis = new FileInputStream(excelFilePath);
      Workbook workbook = WorkbookFactory.create(fis)) {

      Sheet sheet = workbook.getSheet(tipoPago); // Nombre de la hoja
      if (sheet == null) {
        throw new IllegalArgumentException("La hoja AHORRO no existe en el archivo Excel.");
      }

      boolean registroEncontrado = false;

      // Recorremos las filas (a partir de la fila 4, índice 3)
      for (Row row : sheet) {
        if (row.getRowNum() < 3) continue; // Saltar encabezados y filas irrelevantes

        Cell cedulaCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK); // Columna de cédulas
        String cedulaValue = "";

        // Manejar diferentes tipos de celdas
        if (cedulaCell.getCellType() == CellType.NUMERIC) {
          cedulaValue = String.valueOf((long) cedulaCell.getNumericCellValue()); // Convertir numérico a texto
        } else if (cedulaCell.getCellType() == CellType.STRING) {
          cedulaValue = cedulaCell.getStringCellValue();
        }
        if (cedulaValue.equals(cedula)) {
          log.info("Registro encontrado");
          registroEncontrado = true;

          // Encuentra la columna correspondiente a la fecha
          Row headerRow = sheet.getRow(3); // Fila con los encabezados de fecha
          for (Cell cell : headerRow) {
            String cellValue = "";

            if (cell != null) {
              if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue();
              } else if (cell.getCellType() == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                // Convertir fecha numérica a texto en formato "dd/MM/yy"
                Date date = cell.getDateCellValue();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                cellValue = dateFormat.format(date);
              }
            }

            log.info("buscando fecha y comparando con {}: {}", fecha, cellValue);

            // Comparar la celda con la fecha ingresada
            if (cellValue.equals(fecha)) {
              log.info ("fecha encontrada");
              int columnIndex = cell.getColumnIndex();

              // Agregar el monto en la celda correspondiente
              Cell pagoCell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
              log.info ("fecha encontrada {}" , pagoCell.getStringCellValue());
              pagoCell.setCellValue(monto);

              // Actualizar el total en la columna "RECAUDADO"
              Cell totalCell = row.getCell(20, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
              double total = totalCell.getNumericCellValue() + monto;
              totalCell.setCellValue(total);

              break;
            } else {
              log.info("fecha no encontrada");
            }
          }
          break;
        }
      }

      if (!registroEncontrado) {
        throw new IllegalArgumentException("Cédula no encontrada: " + cedula);
      }

      // Escribe los cambios en el archivo Excel
      try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
        workbook.write(fos);
      }
      log.info("Registro creado");
    }
  }

  public static void main (String args[]) throws IOException {

    log.info("creating service");
    FileService file = new FileService();

    file.registrarPago("12777981", "AHORRO", 50.0, "31/07/24");
  }

}
