package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.LogicPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.LogicBalanceResponse;
import com.ajustadoati.sc.adapter.rest.repository.*;
import com.ajustadoati.sc.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
public class LogicBalanceService {
  private final LogicBalanceRepository balances;
  private final LogicPaymentRepository payments;

  public LogicBalanceService(LogicBalanceRepository balances, LogicPaymentRepository payments) {
    this.balances = balances;
    this.payments = payments;
  }

  @Transactional(readOnly = true)
  public LogicBalanceResponse getBalance() {
    return response(balances.findById(1L).orElseThrow(this::missingBalance));
  }

  @Transactional
  public LogicBalanceResponse registerPayment(LogicPaymentRequest request, String actor) {
    // Serialize payments on the shared balance, including retries of the same request.
    var balance = balances.findForUpdate().orElseThrow(this::missingBalance);
    var previous = payments.findByRequestId(request.requestId());
    if (previous.isPresent()) {
      var payment = previous.get();
      if (payment.getAmount().compareTo(request.amount()) != 0
          || !payment.getPaymentDate().equals(request.date())
          || !payment.getCreatedBy().equals(actor)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "El identificador corresponde a otro abono");
      }
      return response(balance);
    }
    if (request.amount().compareTo(balance.getCurrentBalance()) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El abono supera el saldo pendiente");
    }
    balance.setCurrentBalance(balance.getCurrentBalance().subtract(request.amount()));
    var payment = new LogicPayment();
    payment.setRequestId(request.requestId());
    payment.setPaymentDate(request.date());
    payment.setAmount(request.amount());
    payment.setBalanceAfter(balance.getCurrentBalance());
    payment.setCreatedAt(LocalDateTime.now());
    payment.setCreatedBy(actor);
    payments.save(payment);
    balances.save(balance);
    return response(balance);
  }

  private LogicBalanceResponse response(LogicBalance balance) {
    return new LogicBalanceResponse(balance.getInitialBalance(), balance.getCurrentBalance());
  }

  private ResponseStatusException missingBalance() {
    return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El saldo de Logica no esta configurado");
  }
}
