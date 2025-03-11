package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.PaymentFileRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.DailyResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.application.service.PaymentService;
import com.ajustadoati.sc.application.service.file.FileService;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {


  private final PaymentService paymentService;

  private final FileService fileService;

  @PostMapping
  public ResponseEntity<PaymentResponse> processPayments(@RequestBody PaymentRequest request) {
    return ResponseEntity.ok(paymentService.processPayments(request));
  }

  @GetMapping("/report/{date}")
  public ResponseEntity<DailyResponse> dailyReport(@PathVariable LocalDate date) {
    return ResponseEntity.ok(paymentService.generateDailyReport(date));
  }

}
