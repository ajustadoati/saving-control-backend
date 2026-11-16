package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.repository.LubricantOrderRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.adapter.rest.repository.SupplyPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.SupplyRepository;
import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import com.ajustadoati.sc.domain.LubricantOrder;
import com.ajustadoati.sc.domain.Pago;
import com.ajustadoati.sc.domain.Supply;
import com.ajustadoati.sc.domain.SupplyPayment;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.enums.LubricantOrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplyServiceMultipaymentTest {
  @InjectMocks SupplyService service;
  @Mock SupplyRepository supplies;
  @Mock SupplyPaymentRepository payments;
  @Mock PagoRepository cash;
  @Mock LubricantOrderRepository orders;
  @Mock BalanceHistoryService history;

  private final User user = User.builder().userId(1).numberId("test").build();
  private final LocalDate date = LocalDate.of(2026, 11, 16);

  @Test void allocatesOldestFirstAndReversesOnlyLinkedPayments() {
    var newer = supply(1, "120.00", date.minusDays(7));
    var older = supply(2, "80.00", date.minusDays(14));
    var paidOff = supply(3, "0.00", date.minusDays(21));
    var olderOrder = order(older);
    var newerOrder = order(newer);
    when(supplies.lockByUserId(1)).thenReturn(List.of(newer, older, paidOff));
    when(orders.findBySupplyId(1)).thenReturn(Optional.of(newerOrder));
    when(orders.findBySupplyId(2)).thenReturn(Optional.of(olderOrder));
    when(cash.save(any(Pago.class))).thenAnswer(invocation -> {
      var pago = invocation.<Pago>getArgument(0);
      pago.setId(8L);
      return pago;
    });

    service.registerMultipayment(user, null, new BigDecimal("100.00"), date);
    var allocations = ArgumentCaptor.forClass(SupplyPayment.class);
    verify(payments, times(2)).save(allocations.capture());
    assertEquals(List.of(2, 1), allocations.getAllValues().stream().map(p -> p.getSupply().getSupplyId()).toList());
    assertEquals(List.of(new BigDecimal("80.00"), new BigDecimal("20.00")),
      allocations.getAllValues().stream().map(SupplyPayment::getAmount).toList());
    verify(cash).save(argThat(pago -> pago.getTipoPago() == TipoPagoEnum.SUMINISTROS
      && pago.getMonto().equals(new BigDecimal("100.00")) && pago.getFecha().equals(date)));
    assertTrue(allocations.getAllValues().stream().allMatch(p -> p.getMultipaymentPago().getId() == 8L));
    assertEquals(LubricantOrderStatus.PAID, olderOrder.getStatus());
    assertEquals(LubricantOrderStatus.PARTIALLY_PAID, newerOrder.getStatus());

    // A separate payment of 5 has already reduced the newer debt on the same day.
    newer.setSupplyBalance(new BigDecimal("95.00"));
    when(payments.findByMultipaymentPago_IdIn(List.of(8L))).thenReturn(allocations.getAllValues());
    service.reverseMultipayment(user, date, List.of(8L));
    assertEquals(new BigDecimal("80.00"), olderOrder.getBalance());
    assertEquals(new BigDecimal("115.00"), newerOrder.getBalance());
    assertEquals(LubricantOrderStatus.PENDING, olderOrder.getStatus());
    assertEquals(LubricantOrderStatus.PARTIALLY_PAID, newerOrder.getStatus());
    verify(payments).deleteAll(allocations.getAllValues());
    verify(payments, never()).findByUserAndPaymentDate(any(), any());
  }

  @Test void usesIdForDeterministicAllocationOnTheSameDate() {
    var first = supply(1, "30.00", date);
    var second = supply(2, "30.00", date);
    when(supplies.lockByUserId(1)).thenReturn(List.of(second, first));
    when(cash.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
    service.registerMultipayment(user, null, new BigDecimal("10.00"), date);
    assertEquals(new BigDecimal("20.00"), first.getSupplyBalance());
    assertEquals(new BigDecimal("30.00"), second.getSupplyBalance());
  }

  @Test void rejectsOverpaymentBeforeWritingAnything() {
    var debt = supply(2, "10.00", date);
    when(supplies.lockByUserId(1)).thenReturn(List.of(debt));
    assertThrows(IllegalArgumentException.class,
      () -> service.registerMultipayment(user, null, new BigDecimal("10.01"), date));
    assertEquals(new BigDecimal("10.00"), debt.getSupplyBalance());
    verifyNoInteractions(cash, payments, history);
  }

  @Test void rejectsAnIdThatDoesNotBelongToTheUser() {
    when(supplies.lockByUserId(1)).thenReturn(List.of(supply(1, "10.00", date)));
    assertThrows(IllegalArgumentException.class,
      () -> service.registerMultipayment(user, 99, BigDecimal.ONE, date));
    verifyNoInteractions(cash, payments, history);
  }

  private Supply supply(int id, String amount, LocalDate supplyDate) {
    var supply = new Supply();
    supply.setSupplyId(id);
    supply.setSupplyDate(supplyDate);
    supply.setUser(user);
    supply.setSupplyAmount(new BigDecimal(amount));
    supply.setSupplyBalance(supply.getSupplyAmount());
    return supply;
  }

  private LubricantOrder order(Supply supply) {
    var order = new LubricantOrder();
    order.setTotalAmount(supply.getSupplyAmount());
    return order;
  }
}
