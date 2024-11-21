package com.ajustadoati.sc.application.service;


import com.ajustadoati.sc.adapter.rest.dto.request.ContributionPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.ContributionRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentDetail;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse.PaymentStatus;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {

  //private final ContributionPaymentRepository contributionPaymentRepository;
  private final ContributionTypeRepository contributionTypeRepository;
  private final SavingRepository savingRepository;
  private final UserRepository userRepository;
  private final SavingService savingService;
  private final ContributionPaymentService contributionPaymentService;

  public PaymentResponse processPayments(PaymentRequest request) {
    var user = userRepository.findById(request.getUserId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

    List<PaymentStatus> paymentStatuses = new ArrayList<>();
    BigDecimal totalPaid = BigDecimal.ZERO;

    List<SavingRequest> savingRequests = new ArrayList<>();
    List<ContributionPaymentRequest> contributionPaymentRequests = new ArrayList<>();

    for (PaymentDetail paymentDetail : request.getPayments()) {
      PaymentResponse.PaymentStatus status = new PaymentResponse.PaymentStatus();
      status.setPaymentType(paymentDetail.getPaymentType());
      status.setReferenceId(paymentDetail.getReferenceId());
      status.setAmount(paymentDetail.getAmount());

      try {
        switch (paymentDetail.getPaymentType().toUpperCase()) {
          case "CONTRIBUTION":
            contributionPaymentRequests.add(
              getContributionPaymentRequest(user.getUserId(), paymentDetail, request.getDate()));
            break;

          case "SAVING":
            savingRequests.add(
              getSavingRequest(user.getUserId(), paymentDetail, request.getDate()));
            break;

          default:
            throw new IllegalArgumentException("Invalid payment type");
        }

        status.setStatus("SUCCESS");
        status.setMessage("Payment processed successfully");
        totalPaid = totalPaid.add(paymentDetail.getAmount());
      } catch (Exception e) {
        status.setStatus("FAILURE");
        status.setMessage(e.getMessage());
      }

      paymentStatuses.add(status);
    }

    if (!savingRequests.isEmpty()){
      savingService.addSavingSet(request.getUserId(), savingRequests);
    }
    if (!contributionPaymentRequests.isEmpty()) {
      contributionPaymentService.saveList(contributionPaymentRequests);
    }


    PaymentResponse response = new PaymentResponse();
    response.setUserId(user.getUserId());
    response.setTotalPaid(totalPaid);
    response.setPaymentStatuses(paymentStatuses);

    return response;
  }

  private ContributionPaymentRequest getContributionPaymentRequest(Integer userId,
    PaymentDetail paymentDetail, LocalDate date) {

    return ContributionPaymentRequest.builder()
      .contributionId(paymentDetail.getReferenceId())
      .paymentDate(date)
      .amount(paymentDetail.getAmount())
      .userId(Objects.nonNull(paymentDetail.getUserId()) ? paymentDetail.getUserId() : userId)
      .build();

  }

  private SavingRequest getSavingRequest(Integer userId, PaymentDetail paymentDetail,
    LocalDate date) {
    SavingRequest saving = new SavingRequest();
    if (Objects.nonNull(paymentDetail.getUserId())) {
      saving.setAssociateId(paymentDetail.getUserId());
    }

    saving.setAmount(paymentDetail.getAmount());
    saving.setSavingDate(date);

    return saving;
  }
}