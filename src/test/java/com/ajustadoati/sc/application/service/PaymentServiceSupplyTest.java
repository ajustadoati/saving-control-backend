package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.PaymentDetail;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import com.ajustadoati.sc.adapter.rest.repository.DefaultPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceSupplyTest {
  @InjectMocks PaymentService service;
  @Mock UserRepository users;
  @Mock PagoRepository pagos;
  @Mock DefaultPaymentRepository defaults;
  @Mock SupplyService supplies;
  private final User user = User.builder().userId(1).numberId("test-supply").build();
  private final LocalDate date = LocalDate.of(2026, 11, 16);
  private final BigDecimal amount = new BigDecimal("100.00");

  @BeforeEach void setup() {
    when(users.lockForMultipayment(1)).thenReturn(Optional.of(user));
  }

  private PaymentRequest request(PaymentTypeEnum type, Integer referenceId) {
    var detail = new PaymentDetail();
    detail.setPaymentType(type);
    detail.setReferenceId(referenceId);
    detail.setAmount(amount);
    var request = new PaymentRequest();
    request.setUserId(1);
    request.setDate(date);
    request.setPayments(List.of(detail));
    return request;
  }

  @Test void delegatesOneGlobalAmount() {
    var result = service.processPayments(request(PaymentTypeEnum.SUPPLIES_GLOBAL, null));
    assertEquals(amount, result.getTotalPaid());
    assertEquals("SUCCESS", result.getPaymentStatuses().getFirst().getStatus());
    verify(supplies).registerMultipayment(user, null, amount, date);
    verify(pagos, never()).saveAll(argThat(values -> values.iterator().hasNext()));
  }

  @Test void supportsTheExplicitPaymentContract() {
    service.processPayments(request(PaymentTypeEnum.SUPPLIES, 20));
    verify(supplies).registerMultipayment(user, 20, amount, date);
  }

  @Test void blocksTheEntireRequestWhenLegacyDefaultRemains() {
    when(defaults.countLegacySupplyDefaults(user)).thenReturn(1L);
    assertThrows(IllegalArgumentException.class,
      () -> service.processPayments(request(PaymentTypeEnum.SUPPLIES_GLOBAL, null)));
    verifyNoInteractions(pagos, supplies);
  }

  @Test void rejectsUnlinkedLegacyPaymentInsteadOfTreatingItAsGlobal() {
    assertThrows(IllegalArgumentException.class, () -> service.processPayments(request(PaymentTypeEnum.SUPPLIES, null)));
    verifyNoInteractions(supplies);
  }

  @Test void rejectsGlobalMixedWithAnotherSupplyLine() {
    var request = request(PaymentTypeEnum.SUPPLIES_GLOBAL, null);
    request.setPayments(List.of(request.getPayments().getFirst(),
      request(PaymentTypeEnum.SUPPLIES, 20).getPayments().getFirst()));
    assertThrows(IllegalArgumentException.class, () -> service.processPayments(request));
    verifyNoInteractions(pagos, supplies);
  }

  @Test void propagatesInvalidAllocationRatherThanPersistingAPartialMultipayment() {
    doThrow(new IllegalArgumentException("exceeds balance")).when(supplies).registerMultipayment(user, null, amount, date);
    assertThrows(IllegalArgumentException.class,
      () -> service.processPayments(request(PaymentTypeEnum.SUPPLIES_GLOBAL, null)));
    verify(pagos, never()).saveAll(any());
  }
}
