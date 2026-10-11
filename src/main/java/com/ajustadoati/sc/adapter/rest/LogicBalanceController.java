package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.LogicPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.LogicBalanceResponse;
import com.ajustadoati.sc.application.service.LogicBalanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/logic-balance")
public class LogicBalanceController {
  private final LogicBalanceService service;

  public LogicBalanceController(LogicBalanceService service) { this.service = service; }

  @GetMapping
  public LogicBalanceResponse getBalance() { return service.getBalance(); }

  @PostMapping("/payments")
  public LogicBalanceResponse registerPayment(@Valid @RequestBody LogicPaymentRequest request, Principal principal) {
    return service.registerPayment(request, principal.getName());
  }
}
