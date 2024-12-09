package com.ajustadoati.sc.application.service.file;

import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.service.dto.Pago;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Slf4j

public class FileService {

  //private final UserRepository userRepository;

  private final String excelFilePath = "/Users/rojasric/projects/CAJA_AHORRO.xlsx";

  public void registrarPago(String cedula, String tipoPago, double monto, String fecha)
    throws IOException {

    switch (tipoPago) {
      case "AHORRO": {
        registrarPagoAhorro(cedula, tipoPago, monto, fecha);
        break;
      }
      case "COMPARTIR", "ADMINISTRATIVO": {
//        var user =  userRepository.findByNumberId(cedula).get();
//        var nombre = user.getFirstName() + user.getLastName();
        registrarPagoCompartirOAdministrativo(cedula, tipoPago, monto, fecha);
        break;
      }
      default:
        throw new IllegalArgumentException("Not found sheet");
    }
  }

  public void registrarMultiplesPagos(List<Pago> pagos) throws IOException {
    try (FileInputStream fis = new FileInputStream(excelFilePath);
      Workbook workbook = WorkbookFactory.create(fis)) {

      for (Pago pago : pagos) {
        try {
          // Obtener la hoja correspondiente al tipo de pago
          Sheet sheet = workbook.getSheet(pago.getTipoPago());
          if (sheet == null) {
            throw new IllegalArgumentException(
              "Hoja no encontrada para tipo de pago: " + pago.getTipoPago());
          }

          // Manejar el pago según el tipo utilizando switch-case
          switch (pago.getTipoPago().toUpperCase()) {
            case "AHORRO":
              procesarPagoAhorro(sheet, pago);
              break;
            case "COMPARTIR", "ADMINISTRATIVO":
              procesarPagoCompartirOAdministrativo(sheet, pago);
              break;
            case "PRESTAMOS":
              procesarPagoPrestamo(sheet, pago);
              break;
            default:
              throw new IllegalArgumentException("Tipo de pago no válido: " + pago.getTipoPago());
          }
        } catch (Exception e) {
          log.error("Error al procesar el pago para cédula/nombre {}: {}", pago.getCedula(),
            e.getMessage());
        }
      }

      // Escribir todos los cambios en el archivo Excel
      try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
        workbook.write(fos);
      }
    }
    log.info("Todos los pagos han sido procesados y registrados.");
  }


  private void procesarPagoAhorro(Sheet sheet, Pago pago) {
    for (Row row : sheet) {
      if (row.getRowNum() < 3) {
        continue;
      }

      Cell cedulaCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      String cedulaValue = cedulaCell.getCellType() == CellType.STRING
        ? cedulaCell.getStringCellValue()
        : String.valueOf((long) cedulaCell.getNumericCellValue());

      if (cedulaValue.equals(pago.getCedula())) {
        log.info("Registro encontrado para cédula {}", pago.getCedula());
        Row headerRow = sheet.getRow(3);
        for (Cell cell : headerRow) {
          if (esFechaIgual(cell, pago.getFecha())) {
            Cell pagoCell = row.getCell(cell.getColumnIndex(),
              Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            pagoCell.setCellValue(pago.getMonto());
            Cell totalCell = row.getCell(20, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            totalCell.setCellValue(totalCell.getNumericCellValue() + pago.getMonto());
            return;
          }
        }
      }
    }
    throw new IllegalArgumentException("Cédula no encontrada: " + pago.getCedula());
  }

  private void procesarPagoCompartirOAdministrativo(Sheet sheet, Pago pago) {
    for (Row row : sheet) {
      if (row.getRowNum() < 2) {
        continue;
      }

      Cell nombreCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      if (nombreCell.getStringCellValue().equalsIgnoreCase(pago.getCedula())) {
        log.info("Registro encontrado para nombre {}", pago.getCedula());
        Row headerRow = sheet.getRow(2);
        for (Cell cell : headerRow) {
          if (esFechaIgual(cell, pago.getFecha())) {
            Cell pagoCell = row.getCell(cell.getColumnIndex(),
              Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            pagoCell.setCellValue(pago.getMonto());
            return;
          }
        }
      }
    }
    throw new IllegalArgumentException("Nombre no encontrado: " + pago.getCedula());
  }

  private void procesarPagoPrestamo(Sheet sheet, Pago pago) {
    int fechaFilaIndex = -1;
    int fechaColumnaIndex = -1;

    for (Row row : sheet) {
      if (row == null) {
        continue;
      }

      for (Cell cell : row) {
        if (cell != null && cell.getCellType() == CellType.STRING &&
          cell.getStringCellValue().contains("RELACION DE PRESTAMOS") &&
          cell.getStringCellValue().contains(pago.getFecha())) {

          fechaFilaIndex = row.getRowNum();
          fechaColumnaIndex = cell.getColumnIndex();
          break;
        }
      }

      if (fechaFilaIndex != -1) {
        break;
      }
    }

    if (fechaFilaIndex == -1) {
      throw new IllegalArgumentException("Encabezado con fecha no encontrado: " + pago.getFecha());
    }

    log.info("Fila para la fecha {} encontrada en el índice {}", pago.getFecha(), fechaFilaIndex);

    for (int i = fechaFilaIndex + 1; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }

      Cell nombreCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      String nombreValue = nombreCell.getCellType() == CellType.STRING
        ? nombreCell.getStringCellValue()
        : "";

      if (nombreValue.contains(pago.getCedula())) {
        log.info("Registro encontrado para el nombre: {}", pago.getCedula());

        if (pago.getTipoPago().equalsIgnoreCase("abono capital")) {
          Cell abonoCapitalCell = row.getCell(14, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
          double valorActual = abonoCapitalCell.getNumericCellValue();
          abonoCapitalCell.setCellValue(valorActual + pago.getMonto());
        } else if (pago.getTipoPago().equalsIgnoreCase("abono intereses")) {
          Cell abonoInteresesCell = row.getCell(12, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
          double valorActual = abonoInteresesCell.getNumericCellValue();
          abonoInteresesCell.setCellValue(valorActual + pago.getMonto());
        } else {
          throw new IllegalArgumentException(
            "Tipo de pago no válido para préstamos: " + pago.getTipoPago());
        }

        return;
      }
    }
    throw new IllegalArgumentException("Nombre no encontrado: " + pago.getCedula());
  }

  private boolean esFechaIgual(Cell cell, String fecha) {
    if (cell == null) {
      return false;
    }

    if (cell.getCellType() == CellType.STRING) {
      return cell.getStringCellValue().equals(fecha);
    } else if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
      Date cellDate = cell.getDateCellValue();
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
      return dateFormat.format(cellDate).equals(fecha);
    }
    return false;
  }


  public void registrarPagoAhorro(String cedula, String tipoPago, double monto, String fecha)
    throws IOException {
    try (FileInputStream fis = new FileInputStream(excelFilePath);
      Workbook workbook = WorkbookFactory.create(fis)) {

      Sheet sheet = workbook.getSheet(tipoPago); // Nombre de la hoja
      if (sheet == null) {
        throw new IllegalArgumentException("La hoja AHORRO no existe en el archivo Excel.");
      }

      boolean registroEncontrado = false;

      for (Row row : sheet) {
        if (row.getRowNum() < 3) {
          continue;
        }

        Cell cedulaCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        String cedulaValue = "";

        if (cedulaCell.getCellType() == CellType.NUMERIC) {
          cedulaValue = String.valueOf((long) cedulaCell.getNumericCellValue());
        } else if (cedulaCell.getCellType() == CellType.STRING) {
          cedulaValue = cedulaCell.getStringCellValue();
        }
        if (cedulaValue.equals(cedula)) {
          log.info("Registro encontrado");
          registroEncontrado = true;

          Row headerRow = sheet.getRow(3); // Fila con los encabezados de fecha
          for (Cell cell : headerRow) {
            String cellValue = "";

            if (cell != null) {
              if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue();
              } else if (cell.getCellType() == CellType.NUMERIC
                && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                // Convertir fecha numérica a texto en formato "dd/MM/yy"
                Date date = cell.getDateCellValue();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                cellValue = dateFormat.format(date);
              }
            }

            log.info("buscando fecha y comparando con {}: {}", fecha, cellValue);

            // Comparar la celda con la fecha ingresada
            if (cellValue.equals(fecha)) {
              log.info("fecha encontrada");
              int columnIndex = cell.getColumnIndex();

              // Agregar el monto en la celda correspondiente
              Cell pagoCell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
              log.info("fecha encontrada {}", pagoCell.getStringCellValue());
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

  public void registrarPagoCompartirOAdministrativo(String nombreCompleto, String tipoPago,
    double monto, String fecha) throws IOException {

    try (FileInputStream fis = new FileInputStream(excelFilePath);
      Workbook workbook = WorkbookFactory.create(fis)) {

      Sheet sheet = workbook.getSheet(tipoPago); // Nombre de la hoja
      if (sheet == null) {
        throw new IllegalArgumentException(
          "La hoja " + tipoPago + " no existe en el archivo Excel.");
      }

      boolean registroEncontrado = false;

      // Recorremos las filas (a partir de la fila 3, índice 2, según imagen)
      for (Row row : sheet) {
        if (row.getRowNum() < 2) {
          continue; // Saltar encabezados y filas irrelevantes
        }

        Cell nombreCell = row.getCell(1,
          Row.MissingCellPolicy.CREATE_NULL_AS_BLANK); // Columna de nombres
        String nombreValue = nombreCell.getStringCellValue();

        log.info("Nombre: {}", nombreValue);

        if (nombreValue.equalsIgnoreCase(nombreCompleto)) {
          log.info("Registro encontrado para nombre: {}", nombreCompleto);
          registroEncontrado = true;

          // Encuentra la columna correspondiente a la fecha
          Row headerRow = sheet.getRow(2); // Fila con los encabezados de fecha
          for (Cell cell : headerRow) {
            String cellValue = "";

            if (cell != null) {
              if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue();
              } else if (cell.getCellType() == CellType.NUMERIC
                && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                // Convertir fecha numérica a texto en formato "dd/MM/yy"
                Date date = cell.getDateCellValue();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                cellValue = dateFormat.format(date);
              }
            }

            if (cellValue.equals(fecha)) {
              log.info("Fecha encontrada: {}", fecha);
              int columnIndex = cell.getColumnIndex();

              // Agregar el monto en la celda correspondiente
              Cell pagoCell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
              pagoCell.setCellValue(monto);
              break;
            }
          }
          break;
        }
      }

      if (!registroEncontrado) {
        throw new IllegalArgumentException("Nombre no encontrado: " + nombreCompleto);
      }

      // Escribe los cambios en el archivo Excel
      try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
        workbook.write(fos);
      }
      log.info("Pago registrado en la hoja {} para el nombre {}", tipoPago, nombreCompleto);
    }
  }

  public void registrarPagoPrestamo(String hoja, String nombre, double monto,
    String tipoPago, String fecha) throws IOException {

    try (FileInputStream fis = new FileInputStream(excelFilePath);
      Workbook workbook = WorkbookFactory.create(fis)) {

      Sheet sheet = workbook.getSheet(hoja); // Nombre de la hoja
      if (sheet == null) {
        throw new IllegalArgumentException("La hoja " + hoja + " no existe en el archivo Excel.");
      }

      boolean registroEncontrado = false;

      // Buscar la fila del encabezado que contiene "RELACION DE PRESTAMOS <fecha>"
      int fechaFilaIndex = -1;
      int fechaColumnaIndex = -1;

      for (Row row : sheet) {
        if (row == null) {
          continue;
        }

        for (Cell cell : row) {
          if (cell != null && cell.getCellType() == CellType.STRING &&
            cell.getStringCellValue().contains("RELACION DE PRESTAMOS") &&
            cell.getStringCellValue().contains(fecha)) {

            fechaFilaIndex = row.getRowNum(); // Guardar la fila del encabezado
            fechaColumnaIndex = cell.getColumnIndex(); // Guardar la columna de la fecha
            break;
          }
        }

        if (fechaFilaIndex != -1) {
          break; // Salir si encontramos la fila y columna
        }
      }

      if (fechaFilaIndex == -1) {
        throw new IllegalArgumentException("Encabezado con fecha no encontrado: " + fecha);
      }

      log.info("Fila para la fecha {} encontrada en el índice {}", fecha, fechaFilaIndex);

      // Buscar la fila del socio a partir de la fila de la fecha
      for (int i = fechaFilaIndex + 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }

        Cell nombreCell = row.getCell(1,
          Row.MissingCellPolicy.CREATE_NULL_AS_BLANK); // Columna de nombres
        String nombreValue = "";

        // Manejar diferentes tipos de celdas para el nombre
        if (nombreCell.getCellType() == CellType.STRING) {
          nombreValue = nombreCell.getStringCellValue();
        }

        if (nombreValue.contains(nombre)) {
          log.info("Registro encontrado para el nombre: {}", nombre);
          registroEncontrado = true;

          if (tipoPago.equalsIgnoreCase("abono capital")) {
            // Actualizar "Abono Capital"
            Cell abonoCapitalCell = row.getCell(14, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            double valorActual = abonoCapitalCell.getNumericCellValue();
            abonoCapitalCell.setCellValue(valorActual + monto);
          } else if (tipoPago.equalsIgnoreCase("abono intereses")) {
            // Actualizar "Abono Intereses"
            Cell abonoInteresesCell = row.getCell(12, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            double valorActual = abonoInteresesCell.getNumericCellValue();
            abonoInteresesCell.setCellValue(valorActual + monto);
          } else {
            throw new IllegalArgumentException("Tipo de pago no válido: " + tipoPago);
          }

          break;
        }
      }

      if (!registroEncontrado) {
        throw new IllegalArgumentException("Nombre no encontrado: " + nombre);
      }

      // Escribir los cambios en el archivo Excel
      try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
        workbook.write(fos);
      }
      log.info("Pago registrado en la hoja {} para el nombre {}, tipo de pago: {}", hoja, nombre,
        tipoPago);
    }
  }


  public static void main(String args[]) throws IOException {

    log.info("creating service");
    FileService file = new FileService();

    // file.registrarPago("JESUS GUILLERMO PUENTE MONTILLA", "ADMINISTRATIVO", 50.0, "/07/24");
    file.registrarPagoPrestamo("PRESTAMOS", "JOSE LUIS ZAMBRANO", 11.0, "Abono capital",
      "07/08/24");
  }

}
