package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.*;
import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyResponse;
import com.ajustadoati.sc.adapter.rest.repository.*;
import com.ajustadoati.sc.application.mapper.PagoMapper;
import com.ajustadoati.sc.domain.Pago;
import com.ajustadoati.sc.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceSupplyTest {
  @InjectMocks PaymentService service;
  @Mock UserRepository users;
  @Mock PagoRepository pagos;
  @Mock PagoMapper mapper;
  @Mock SupplyService supplies;
  @Mock LubricantOrderRepository orders;

  @BeforeEach void setup() {
    var user = new User();
    user.setUserId(1);
    user.setNumberId("test-supply");
    when(users.findById(1)).thenReturn(Optional.of(user));
  }

  private PaymentRequest request(Integer referenceId) {
    var detail = new PaymentDetail();
    detail.setPaymentType(PaymentTypeEnum.SUPPLIES);
    detail.setReferenceId(referenceId);
    detail.setAmount(new BigDecimal("10.00"));
    var request = new PaymentRequest();
    request.setUserId(1);
    request.setDate(LocalDate.of(2026, 10, 28));
    request.setPayments(List.of(detail));
    return request;
  }

  private SupplyResponse supply(int id) {
    var response = new SupplyResponse();
    response.setSupplyId(id);
    response.setSupplyBalance(new BigDecimal("100.00"));
    return response;
  }

  @Test void paysTheExplicitDebtInsteadOfTheFirst() {
    when(supplies.getSuppliesByUser(1)).thenReturn(List.of(supply(10), supply(20)));
    when(mapper.toEntity(any())).thenReturn(new Pago());
    var result = service.processPayments(request(20));
    assertEquals("SUCCESS", result.getPaymentStatuses().getFirst().getStatus());
    verify(supplies).registerPayment(argThat(payment -> payment.getSupplyId()==20
      && payment.getAmount().compareTo(new BigDecimal("10.00"))==0));
  }

  @Test void failedDebtDoesNotCreateIncome() {
    when(supplies.getSuppliesByUser(1)).thenReturn(List.of(supply(10)));
    doThrow(new IllegalArgumentException("exceeds balance")).when(supplies).registerPayment(any());
    var result = service.processPayments(request(10));
    assertEquals("FAILURE", result.getPaymentStatuses().getFirst().getStatus());
    assertEquals(0, result.getTotalPaid().signum());
    verifyNoInteractions(mapper);
  }

  @Test void refusesAnotherUsersDebt() {
    when(supplies.getSuppliesByUser(1)).thenReturn(List.of(supply(10)));
    var result = service.processPayments(request(99));
    assertEquals("FAILURE", result.getPaymentStatuses().getFirst().getStatus());
    verify(supplies, never()).registerPayment(any());
    verifyNoInteractions(mapper);
  }

  @Test void keepsUnlinkedLegacyDefaultsWithoutCreatingDebt() {
    when(supplies.getSuppliesByUser(1)).thenReturn(List.of());
    when(mapper.toEntity(any())).thenReturn(new Pago());
    var result = service.processPayments(request(null));
    assertEquals("SUCCESS", result.getPaymentStatuses().getFirst().getStatus());
    verify(supplies, never()).registerPayment(any());
    verify(mapper).toEntity(any());
  }

  @Test void legacyDefaultNeverPaysAFinancedOrderImplicitly() {
    when(supplies.getSuppliesByUser(1)).thenReturn(List.of(supply(10)));
    when(orders.findBySupplyId(10)).thenReturn(Optional.of(new com.ajustadoati.sc.domain.LubricantOrder()));
    when(mapper.toEntity(any())).thenReturn(new Pago());
    var result = service.processPayments(request(null));
    assertEquals("SUCCESS", result.getPaymentStatuses().getFirst().getStatus());
    verify(supplies, never()).registerPayment(any());
  }
}
