package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.LoanPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LoanRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.LoanPaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LoanResponse;
import com.ajustadoati.sc.application.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

  private final LoanService loanService;

  @PostMapping
  public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanRequest request) {
    return ResponseEntity.ok(loanService.createLoan(request));
  }

  @PostMapping("/payments")
  public ResponseEntity<Void> registerPayment(@RequestBody LoanPaymentRequest request) {
    loanService.registerPayment(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<LoanResponse>> getLoansByUser(@PathVariable Integer userId) {
    return ResponseEntity.ok(loanService.getLoansByUser(userId));
  }

  @GetMapping("/{loanId}/payments")
  public ResponseEntity<List<LoanPaymentResponse>> getPaymentsByLoan(@PathVariable Integer loanId) {
    return ResponseEntity.ok(loanService.getPaymentsByLoan(loanId));
  }

}
