package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.SupplyPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyPaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyResponse;
import com.ajustadoati.sc.adapter.rest.repository.LubricantOrderRepository;
import com.ajustadoati.sc.adapter.rest.repository.SupplyPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.SupplyRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.application.mapper.SupplyPaymentMapper;
import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import com.ajustadoati.sc.domain.Pago;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.Supply;
import com.ajustadoati.sc.domain.SupplyPayment;
import com.ajustadoati.sc.domain.enums.LubricantOrderStatus;
import com.ajustadoati.sc.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

  private final SupplyRepository supplyRepository;
  private final PagoRepository pagoRepository;
  private final SupplyPaymentRepository supplyPaymentRepository;
  private final UserService userService;
  private final BalanceHistoryService balanceHistoryService;
  private final SupplyPaymentMapper supplyPaymentMapper;
  private final LubricantOrderRepository lubricantOrderRepository;

  public SupplyResponse createSupply(SupplyRequest request) {

    var user = userService.getUserById(request.getUserId());
    Supply supply = new Supply();
    supply.setUser(user);
    supply.setSupplyAmount(request.getSupplyAmount());
    supply.setSupplyBalance(request.getSupplyAmount());
    supply.setSupplyName(request.getSupplyName());
    supply.setSupplyDate(request.getSupplyDate());

    supply = supplyRepository.save(supply);
    return mapToResponse(supply);
  }

  @Transactional(noRollbackFor = IllegalArgumentException.class)
  public void registerPayment(SupplyPaymentRequest request) {
    validatePayment(request.getAmount(), request.getPaymentDate());
    var supply = supplyRepository.findForPaymentById(request.getSupplyId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid Supply"));

    if (request.getAmount().compareTo(supply.getSupplyBalance()) > 0) {
      throw new IllegalArgumentException("Payment amount exceeds supply balance");
    }
    applyPayment(supply, request.getAmount(), request.getPaymentDate(), null);
  }

  void registerMultipayment(User user, Integer referenceId, BigDecimal amount, LocalDate date) {
    validatePayment(amount, date);
    var debts = supplyRepository.lockByUserId(user.getUserId()).stream()
      .filter(supply -> supply.getSupplyBalance().signum() > 0)
      .filter(supply -> referenceId == null || referenceId.equals(supply.getSupplyId()))
      .sorted(Comparator.comparing(Supply::getSupplyDate).thenComparing(Supply::getSupplyId))
      .toList();
    var total = debts.stream().map(Supply::getSupplyBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (amount.compareTo(total) > 0) {
      throw new IllegalArgumentException("El abono supera el saldo pendiente de suministros");
    }

    var pago = pagoRepository.save(Pago.builder()
      .cedula(user.getNumberId()).tipoPago(TipoPagoEnum.SUMINISTROS).fecha(date).monto(amount).build());
    var remaining = amount;
    for (var debt : debts) {
      if (remaining.signum() == 0) break;
      var applied = remaining.min(debt.getSupplyBalance());
      applyPayment(debt, applied, date, pago);
      remaining = remaining.subtract(applied);
    }
  }

  private void validatePayment(BigDecimal amount, LocalDate date) {
    if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2 || date == null) {
      throw new IllegalArgumentException("Indica fecha y monto positivo con hasta dos decimales");
    }
  }

  private void applyPayment(Supply supply, BigDecimal amount, LocalDate date, Pago pago) {
    SupplyPayment payment = new SupplyPayment();
    payment.setSupply(supply);
    payment.setPaymentDate(date);
    payment.setAmount(amount);
    payment.setMultipaymentPago(pago);

    supply.setSupplyBalance(supply.getSupplyBalance().subtract(amount));
    supplyPaymentRepository.save(payment);
    balanceHistoryService.save(new BalanceHistoryDto(0, supply.getUser().getUserId(), date,
      TransactionType.SUPPLIES, amount, "Payment supplies"));
    supplyRepository.save(supply);
    syncOrderBalance(supply);
  }

  void reverseMultipayment(User user, LocalDate date, List<Long> pagoIds) {
    supplyRepository.lockByUserId(user.getUserId());
    var payments = supplyPaymentRepository.findByMultipaymentPago_IdIn(pagoIds);
    if (payments.isEmpty()) {
      // Historical multipayments did not store a link to their allocations.
      payments = supplyPaymentRepository.findByUserAndPaymentDate(user, date);
    }
    reversePayments(payments);
  }

  private void syncOrderBalance(Supply supply) {
    lubricantOrderRepository.findBySupplyId(supply.getSupplyId()).ifPresent(order -> {
      var balance = supply.getSupplyBalance();
      order.setBalance(balance);
      order.setStatus(balance.signum() == 0 ? LubricantOrderStatus.PAID
        : balance.compareTo(order.getTotalAmount()) < 0 ? LubricantOrderStatus.PARTIALLY_PAID
        : LubricantOrderStatus.PENDING);
      lubricantOrderRepository.save(order);
    });
  }

  @Transactional
  public void reversePayments(List<SupplyPayment> supplyPayments) {
    if (supplyPayments == null || supplyPayments.isEmpty()) {
      return;
    }

    for (var payment : supplyPayments) {
      var supply = payment.getSupply();
      supply.setSupplyBalance(supply.getSupplyBalance().add(payment.getAmount()));
      supplyRepository.save(supply);

      syncOrderBalance(supply);
    }

    supplyPaymentRepository.deleteAll(supplyPayments);
  }

  public List<SupplyResponse> getSuppliesByUser(Integer userId) {
    return supplyRepository.findByUser_UserId(userId).stream()
      .map(this::mapToResponse).toList();
  }

  public List<SupplyPaymentResponse> getPaymentsBySupply(Integer supplyId) {
    return supplyPaymentRepository.findBySupply_SupplyId(supplyId).stream()
      .map(supplyPaymentMapper::toResponse).toList();
  }

  private SupplyResponse mapToResponse(Supply supply) {
    SupplyResponse response = new SupplyResponse();
    response.setSupplyId(supply.getSupplyId());
    response.setSupplyAmount(supply.getSupplyAmount());
    response.setSupplyName(supply.getSupplyName());
    response.setSupplyBalance(supply.getSupplyBalance());
    response.setSupplyDate(supply.getSupplyDate());
    return response;
  }
}
