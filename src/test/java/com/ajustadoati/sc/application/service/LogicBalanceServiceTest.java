package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.LogicPaymentRequest;
import com.ajustadoati.sc.adapter.rest.repository.*;
import com.ajustadoati.sc.domain.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogicBalanceServiceTest {
  private final LogicBalanceRepository balances = mock(LogicBalanceRepository.class);
  private final LogicPaymentRepository payments = mock(LogicPaymentRepository.class);
  private final LogicBalanceService service = new LogicBalanceService(balances, payments);
  private LogicBalance balance;
  private final String requestId = "fe71fbc3-58d3-463f-9349-359d9e202bb4";

  @BeforeEach
  void setup() {
    balance = new LogicBalance();
    balance.setId(1L);
    balance.setInitialBalance(new BigDecimal("19524.00"));
    balance.setCurrentBalance(new BigDecimal("19524.00"));
    when(balances.findForUpdate()).thenReturn(Optional.of(balance));
    when(payments.findByRequestId(requestId)).thenReturn(Optional.empty());
  }

  private LogicPaymentRequest request(String amount) {
    return new LogicPaymentRequest(LocalDate.now(), new BigDecimal(amount), requestId);
  }

  @Test
  void paymentReducesSharedBalanceAndRetainsAuditData() {
    var result = service.registerPayment(request("150.25"), "admin");
    assertEquals(new BigDecimal("19373.75"), result.currentBalance());
    assertEquals(new BigDecimal("19524.00"), result.initialBalance());
    var captor = ArgumentCaptor.forClass(LogicPayment.class);
    verify(payments).save(captor.capture());
    assertEquals("admin", captor.getValue().getCreatedBy());
    assertEquals(LocalDate.now(), captor.getValue().getPaymentDate());
    assertEquals(result.currentBalance(), captor.getValue().getBalanceAfter());
  }

  @Test
  void exactPaymentCanSetBalanceToZero() {
    assertEquals(new BigDecimal("0.00"), service.registerPayment(request("19524.00"), "admin").currentBalance());
  }

  @Test
  void overpaymentDoesNotModifyBalanceOrCreatePayment() {
    assertThrows(ResponseStatusException.class, () -> service.registerPayment(request("19524.01"), "admin"));
    assertEquals(new BigDecimal("19524.00"), balance.getCurrentBalance());
    verify(payments, never()).save(any());
    verify(balances, never()).save(any());
  }

  @Test
  void retryDoesNotSubtractAgain() {
    var previous = new LogicPayment();
    previous.setAmount(new BigDecimal("150.25"));
    previous.setPaymentDate(LocalDate.now());
    previous.setCreatedBy("admin");
    when(payments.findByRequestId(requestId)).thenReturn(Optional.of(previous));
    balance.setCurrentBalance(new BigDecimal("19373.75"));
    assertEquals(new BigDecimal("19373.75"), service.registerPayment(request("150.25"), "admin").currentBalance());
    verify(payments, never()).save(any());
    verify(balances, never()).save(any());
    assertThrows(ResponseStatusException.class, () -> service.registerPayment(request("100.00"), "admin"));
  }

  @Test
  void requestValidationRejectsNonpositiveAmountsExtraDecimalsAndFutureDates() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertTrue(validator.validate(request("150.25")).isEmpty());
      assertFalse(validator.validate(request("0")).isEmpty());
      assertFalse(validator.validate(request("-1")).isEmpty());
      assertFalse(validator.validate(request("1.001")).isEmpty());
      assertFalse(validator.validate(new LogicPaymentRequest(LocalDate.now().plusDays(1), BigDecimal.ONE, requestId)).isEmpty());
    }
  }
}
