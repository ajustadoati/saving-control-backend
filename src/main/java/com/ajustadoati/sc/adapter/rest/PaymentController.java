package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.application.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {


  private final PaymentService paymentService;

  @PostMapping
  public ResponseEntity<PaymentResponse> processPayments(@RequestBody PaymentRequest request) {
    return ResponseEntity.ok(paymentService.processPayments(request));
  }
}
