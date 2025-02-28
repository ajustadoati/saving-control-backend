package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.SupplyPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyResponse;
import com.ajustadoati.sc.adapter.rest.repository.SupplyPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.SupplyRepository;
import com.ajustadoati.sc.domain.Supply;
import com.ajustadoati.sc.domain.SupplyPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplyService {

  private final SupplyRepository supplyRepository;
  private final SupplyPaymentRepository supplyPaymentRepository;
  private final UserService userService;

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

  public void registerPayment(SupplyPaymentRequest request) {
    var supply = supplyRepository.findById(request.getSupplyId())
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
    supplyRepository.save(supply);
  }

  public List<SupplyResponse> getSuppliesByUser(Integer userId) {
    return supplyRepository.findByUser_UserId(userId).stream()
      .map(this::mapToResponse).toList();
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
