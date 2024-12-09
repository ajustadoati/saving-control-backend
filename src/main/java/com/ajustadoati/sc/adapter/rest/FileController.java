package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.response.FileResponse;
import com.ajustadoati.sc.application.service.ExcelFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

  private final ExcelFileService excelFileService;

  @PostMapping("/upload")
  public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      var response = excelFileService.uploadFile(file);
      return ResponseEntity.ok(response);
    } catch (IOException e) {
      log.info("Error uploading file");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FileResponse.builder()
        .message("Error uploading file").build());
    }
  }

  @GetMapping("/download")
  public ResponseEntity<Resource> downloadFile() {
    return excelFileService.downloadAndDeleteFile();
  }

}
