package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.SupplyPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyPaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyResponse;
import com.ajustadoati.sc.application.service.SupplyService;
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
@RequestMapping("/api/supplies")
@RequiredArgsConstructor
public class SupplyController {

  private final SupplyService supplyService;

  @PostMapping
  public ResponseEntity<SupplyResponse> createSupply(@RequestBody SupplyRequest request) {
    return ResponseEntity.ok(supplyService.createSupply(request));
  }

  @PostMapping("/payments")
  public ResponseEntity<Void> registerPayment(@RequestBody SupplyPaymentRequest request) {
    supplyService.registerPayment(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<SupplyResponse>> getSuppliesByUser(@PathVariable Integer userId) {
    return ResponseEntity.ok(supplyService.getSuppliesByUser(userId));
  }

  @GetMapping("/{supplyId}/payments")
  public ResponseEntity<List<SupplyPaymentResponse>> getPaymentsBySupply(@PathVariable Integer supplyId) {
    return ResponseEntity.ok(supplyService.getPaymentsBySupply(supplyId));
  }
}
