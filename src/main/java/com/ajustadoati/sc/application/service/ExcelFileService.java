package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.FileResponse;
import com.ajustadoati.sc.config.properties.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelFileService {


  private final FileProperties fileProperties;

  private static final String FILE_NAME = "CAJA_AHORRO.xlsx";

  public FileResponse uploadFile(MultipartFile file) throws IOException {
    var fileLocal = new File(fileProperties.getDir(), FILE_NAME);

    if (fileLocal.exists()) {
      throw new IllegalArgumentException("Already File uploaded");
    }

    file.transferTo(fileLocal);

    return FileResponse.builder().message("File uploaded successfully").build();
  }

  public ResponseEntity<Resource> downloadAndDeleteFile() {
    File file = new File(fileProperties.getDir(), FILE_NAME);

    if (!file.exists()) {
      throw new IllegalArgumentException("File not found or it was downloaded previously");
    }

    try {
      InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
      ResponseEntity<Resource> response = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(file.length())
        .body(resource);

      Files.delete(file.toPath());
      log.info("Archivo eliminado tras la descarga: " + file.getName());

      return response;
    } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().build();
    }
  }

}
