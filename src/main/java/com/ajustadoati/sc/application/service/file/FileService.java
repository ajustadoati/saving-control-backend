package com.ajustadoati.sc.application.service.file;

import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.service.ExcelFileService;
import com.ajustadoati.sc.application.service.dto.PagoDto;
import com.ajustadoati.sc.domain.User;
import jakarta.transaction.Transactional;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

  private final UserRepository userRepository;
  private final ExcelFileService excelFileService;

  //private final String excelFilePath = "/Users/rojasric/projects/CAJA_AHORRO.xlsx";


  public String cambiarFormatoFecha(String fechaActual) {

    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");
    LocalDate date = LocalDate.parse(fechaActual, inputFormatter);

    return date.format(outputFormatter);
  }

  @Transactional
  public void registrarMultiplesPagos(List<PagoDto> pagoDtos, User user) throws IOException {

    try (FileInputStream fis = new FileInputStream(excelFileService.filePath());
      Workbook workbook = WorkbookFactory.create(fis)) {

      for (PagoDto pagoDto : pagoDtos) {
        try {
          var sheetName = pagoDto.getTipoPago().getCategory();
          // Obtener la hoja correspondiente al tipo de pago
          Sheet sheet = workbook.getSheet(sheetName);
          if (sheet == null) {
            throw new IllegalArgumentException(
              "Hoja no encontrada para tipo de pago: " + pagoDto.getTipoPago());
          }

          pagoDto.setFecha(cambiarFormatoFecha(pagoDto.getFecha()));

          // Manejar el pago según el tipo utilizando switch-case
          switch (pagoDto.getTipoPago().getCategory()) {
            case "AHORRO":
              /*long number = Long.parseLong(pago.getCedula());
              NumberFormat numberFormat = NumberFormat.getInstance(Locale.of("es", "CO"));
              pago.setCedula(numberFormat.format(number));*/
              procesarPagoAhorro(sheet, pagoDto);
              break;
            case "COMPARTIR", "ADMINISTRATIVO":
              procesarPagoCompartirOAdministrativo(sheet, pagoDto, user);
              break;
            case "PRESTAMOS":
              procesarPagoPrestamo(sheet, pagoDto, user);
              break;

            default:
              throw new IllegalArgumentException("Tipo de pago no válido: " + pagoDto.getTipoPago());
          }
        } catch (Exception e) {
          log.error("Error al procesar el pago para {}: {}", user.getFirstName() ,
            e.getMessage());
        }
      }

      // Escribir todos los cambios en el archivo Excel
      try (FileOutputStream fos = new FileOutputStream(excelFileService.filePath())) {
        workbook.write(fos);
      }
    }
    log.info("Todos los pagos han sido procesados y registrados.");
  }


  private void procesarPagoAhorro(Sheet sheet, PagoDto pagoDto) {
    log.info("procesando pago ahorro {}", pagoDto);
    for (Row row : sheet) {
      if (row.getRowNum() < 3) {
        continue;
      }

      Cell cedulaCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      String cedulaValue = cedulaCell.getCellType() == CellType.STRING
        ? cedulaCell.getStringCellValue()
        : String.valueOf((long) cedulaCell.getNumericCellValue());

      if (cedulaValue.contains(pagoDto.getCedula())) {
        log.info("Registro encontrado para cédula {}", pagoDto.getCedula());
        Row headerRow = sheet.getRow(3);
        for (Cell cell : headerRow) {
          if (esFechaIgual(cell, pagoDto.getFecha())) {
            Cell pagoCell = row.getCell(cell.getColumnIndex(),
              Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            pagoCell.setCellValue(pagoDto.getMonto());
            Cell totalCell = row.getCell(20, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            totalCell.setCellValue(totalCell.getNumericCellValue() + pagoDto.getMonto());
            log.info("Pago procesado satisfactoriamente");
            return;
          }
        }
      }
    }
    throw new IllegalArgumentException("Cédula no encontrada: " + pagoDto.getCedula());
  }

  private void procesarPagoCompartirOAdministrativo(Sheet sheet, PagoDto pagoDto, User user) {
    log.info("procesando pago compartir o administrativo {}", pagoDto);
    for (Row row : sheet) {
      if (row.getRowNum() < 2) {
        continue;
      }

      Cell nombreCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      if (nombreCell.getStringCellValue()
        .contains(user.getFirstName() + " " + user.getLastName())) {
        log.info("Registro encontrado para nombre {}",
          user.getFirstName() + " " + user.getLastName());
        Row headerRow = sheet.getRow(2);
        for (Cell cell : headerRow) {
          if (esFechaIgual(cell, pagoDto.getFecha())) {
            Cell pagoCell = row.getCell(cell.getColumnIndex(),
              Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            pagoCell.setCellValue(pagoDto.getMonto());
            log.info("Pago procesado: {}", pagoDto.getTipoPago().getDescription());
            return;
          }
        }
      }
    }
    throw new IllegalArgumentException("Nombre no encontrado: " + pagoDto.getCedula());
  }

  private void procesarPagoPrestamo(Sheet sheet, PagoDto pagoDto, User user) {
    log.info("Procesando pago préstamo {}", pagoDto);

    int fechaFilaIndex = -1;

    // Encontrar la fila del encabezado con la fecha
    for (Row row : sheet) {
      if (row == null) continue;

      for (Cell cell : row) {
        if (cell != null && cell.getCellType() == CellType.STRING &&
          cell.getStringCellValue().contains("RELACION DE PRESTAMOS") &&
          cell.getStringCellValue().contains(pagoDto.getFecha())) {

          fechaFilaIndex = row.getRowNum();
          break;
        }
      }
      if (fechaFilaIndex != -1) break;
    }

    if (fechaFilaIndex == -1) {
      throw new IllegalArgumentException("Encabezado con fecha no encontrado: " + pagoDto.getFecha());
    }

    log.info("Fila para la fecha '{}' encontrada en el índice {}", pagoDto.getFecha(), fechaFilaIndex);

    // Buscar la fila del usuario
    boolean usuarioEncontrado = false;

    for (int i = fechaFilaIndex + 1; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) continue;

      Cell nombreCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      String nombreValue = nombreCell.getCellType() == CellType.STRING
        ? nombreCell.getStringCellValue().trim()
        : "";

      if (nombreValue.isEmpty()) {
        continue; // Ignorar celdas vacías en la columna de nombres
      }

      log.info("Verificando fila: {}, nombre en celda: {}", row.getRowNum(), nombreValue);

      if (esNombreEncontrado(nombreValue.split(" "), user.getFirstName() + " " + user.getLastName())) {
        log.info("Registro encontrado para el nombre: {} en la fila {}", user.getFirstName() + " " + user.getLastName(), row.getRowNum());

        // Determinar columna según tipo de pago
        int columna = pagoDto.getTipoPago().getDescription().equalsIgnoreCase("abono capital") ? 14 : 12;
        actualizarCelda(row, columna, pagoDto.getMonto());

        usuarioEncontrado = true;
        break;
      }
    }

    if (!usuarioEncontrado) {
      throw new IllegalArgumentException("Usuario no encontrado: " + user.getFirstName() + " " + user.getLastName());
    }
  }

  private void actualizarCelda(Row row, int columnIndex, double monto) {
    Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

    if (cell.getCellType() != CellType.NUMERIC) {
      log.warn("Celda en columna {} no es numérica. Inicializando...", columnIndex);
      cell.setCellType(CellType.NUMERIC);
      cell.setCellValue(0.0); // Inicializar como 0 si contiene texto
    }

    double valorActual = cell.getNumericCellValue();
    log.info("Valor actual en la celda: {}, Monto a agregar: {}, en fila: {}", valorActual, monto, row.getRowNum());
    cell.setCellValue(valorActual + monto);
  }



  /**
   * Compara el nombre encontrado en la celda con el nombre completo del usuario.
   */
  private boolean esNombreEncontrado(String[] nombresCelda, String nombreCompleto) {

    if (nombresCelda == null || nombresCelda.length == 0 || nombresCelda[0].trim().isEmpty()) {
      return false; // Ignorar celdas vacías
    }

    String nombreFormateado = nombreCompleto.toLowerCase().trim();

    for (String palabra : nombresCelda) {
      if (!nombreFormateado.contains(palabra.toLowerCase().trim())) {
        System.out.println("Nombre no coincide: " + palabra + " no está en " + nombreCompleto);
        return false;
      }
    }

    System.out.println("Nombre encontrado: " + nombreCompleto);
    return true;
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
    try (FileInputStream fis = new FileInputStream(excelFileService.filePath());
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
      try (FileOutputStream fos = new FileOutputStream(excelFileService.filePath())) {
        workbook.write(fos);
      }
      log.info("Registro creado");
    }
  }

  public void registrarPagoCompartirOAdministrativo(String nombreCompleto, String tipoPago,
    double monto, String fecha) throws IOException {

    try (FileInputStream fis = new FileInputStream(excelFileService.filePath());
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
      try (FileOutputStream fos = new FileOutputStream(excelFileService.filePath())) {
        workbook.write(fos);
      }
      log.info("Pago registrado en la hoja {} para el nombre {}", tipoPago, nombreCompleto);
    }
  }

  public void registrarPagoPrestamo(Sheet sheet, PagoDto pagoDto, User user) throws IOException {

    try (FileInputStream fis = new FileInputStream(excelFileService.filePath());
      Workbook workbook = WorkbookFactory.create(fis)) {

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
            cell.getStringCellValue().contains(pagoDto.getFecha())) {

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
        throw new IllegalArgumentException("Encabezado con fecha no encontrado: " + pagoDto.getFecha());
      }

      log.info("Fila para la fecha {} encontrada en el índice {}",  pagoDto.getFecha(), fechaFilaIndex);

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

        if (esNombreEncontrado(nombreValue.split(" "), user.getFirstName() + " "+ user.getLastName())) {
          log.info("Registro encontrado para el nombre: {}", user.getFirstName() + " "+ user.getLastName());
          registroEncontrado = true;

          if (pagoDto.getTipoPago().getDescription().equalsIgnoreCase("abono capital")) {
            // Actualizar "Abono Capital"
            Cell abonoCapitalCell = row.getCell(14, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            double valorActual = abonoCapitalCell.getNumericCellValue();
            abonoCapitalCell.setCellValue(valorActual + pagoDto.getMonto());
          } else if (pagoDto.getTipoPago().getDescription().equalsIgnoreCase("abono intereses")) {
            // Actualizar "Abono Intereses"
            Cell abonoInteresesCell = row.getCell(12, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            double valorActual = abonoInteresesCell.getNumericCellValue();
            abonoInteresesCell.setCellValue(valorActual + pagoDto.getMonto());
          } else {
            throw new IllegalArgumentException("Tipo de pago no válido: {}" + pagoDto);
          }

          break;
        }
      }

      if (!registroEncontrado) {
        throw new IllegalArgumentException("Nombre no encontrado: " + user.getFirstName());
      }

      // Escribir los cambios en el archivo Excel
      try (FileOutputStream fos = new FileOutputStream(excelFileService.filePath())) {
        workbook.write(fos);
      }
      log.info("Pago registrado en la hoja {} para el nombre {}, tipo de pago: {}", sheet, user.getFirstName(),
        pagoDto.getTipoPago());
    }
  }


  /*public static void main(String args[]) throws IOException {

    log.info("creating service");
    FileService file = new FileService();

    // file.registrarPago("JESUS GUILLERMO PUENTE MONTILLA", "ADMINISTRATIVO", 50.0, "/07/24");
    file.registrarPagoPrestamo("PRESTAMOS", "JOSE LUIS ZAMBRANO", 11.0, "Abono capital",
      "07/08/24");
  }*/

}
