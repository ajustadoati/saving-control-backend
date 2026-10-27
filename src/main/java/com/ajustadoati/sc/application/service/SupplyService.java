package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.SupplyPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyPaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyResponse;
import com.ajustadoati.sc.adapter.rest.repository.LubricantOrderRepository;
import com.ajustadoati.sc.adapter.rest.repository.SupplyPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.SupplyRepository;
import com.ajustadoati.sc.application.mapper.SupplyPaymentMapper;
import com.ajustadoati.sc.domain.BalanceHistory;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

  private final SupplyRepository supplyRepository;
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
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0
        || request.getAmount().stripTrailingZeros().scale() > 2 || request.getPaymentDate() == null) {
      throw new IllegalArgumentException("Indica fecha y monto positivo con hasta dos decimales");
    }
    var supply = supplyRepository.findForPaymentById(request.getSupplyId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid Supply"));

    if (request.getAmount().compareTo(supply.getSupplyBalance()) > 0) {
      throw new IllegalArgumentException("Payment amount exceeds supply balance");
    }

    SupplyPayment payment = new SupplyPayment();
    payment.setSupply(supply);
    payment.setPaymentDate(request.getPaymentDate());
    payment.setAmount(request.getAmount());

    supply.setSupplyBalance(supply.getSupplyBalance().subtract(request.getAmount()));

    supplyPaymentRepository.save(payment);
    var history = new BalanceHistoryDto(0, supply.getUser().getUserId(), request.getPaymentDate(),
      TransactionType.SUPPLIES, request.getAmount(), "Payment supplies");
    balanceHistoryService.save(history);
    supplyRepository.save(supply);
    lubricantOrderRepository.findBySupplyId(supply.getSupplyId()).ifPresent(order -> {
      order.setBalance(supply.getSupplyBalance());
      if (supply.getSupplyBalance().compareTo(java.math.BigDecimal.ZERO) == 0) {
        order.setStatus(LubricantOrderStatus.PAID);
      } else if (supply.getSupplyBalance().compareTo(order.getTotalAmount()) < 0) {
        order.setStatus(LubricantOrderStatus.PARTIALLY_PAID);
      } else {
        order.setStatus(LubricantOrderStatus.PENDING);
      }
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

      lubricantOrderRepository.findBySupplyId(supply.getSupplyId()).ifPresent(order -> {
        order.setBalance(supply.getSupplyBalance());
        if (supply.getSupplyBalance().compareTo(BigDecimal.ZERO) == 0) {
          order.setStatus(LubricantOrderStatus.PAID);
        } else if (supply.getSupplyBalance().compareTo(order.getTotalAmount()) < 0) {
          order.setStatus(LubricantOrderStatus.PARTIALLY_PAID);
        } else {
          order.setStatus(LubricantOrderStatus.PENDING);
        }
        lubricantOrderRepository.save(order);
      });
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
