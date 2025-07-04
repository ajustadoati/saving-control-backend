package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionPaymentRequest;
import com.ajustadoati.sc.adapter.rest.repository.ContributionPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.BalanceHistory;
import com.ajustadoati.sc.domain.Contribution;
import com.ajustadoati.sc.domain.ContributionPayment;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.enums.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionPaymentService {

  private final ContributionPaymentRepository contributionPaymentRepository;

  private final UserRepository userRepository;

  private final ContributionService contributionService;

  private final BalanceHistoryService balanceHistoryService;

  public ContributionPayment save(ContributionPaymentRequest request) {
    userRepository.findById(request.getUserId()).orElseThrow();
    return contributionPaymentRepository.save(toEntity(request));
  }

  @Transactional
  public List<ContributionPayment> saveList(List<ContributionPaymentRequest> requestList) {
    userRepository.findById(requestList.getFirst().getUserId()).orElseThrow();

    var invalidContributionIds = requestList.stream()
      .map(ContributionPaymentRequest::getContributionId)
      .filter(contributionId -> !contributionService.existsById(contributionId))
      .toList();

    if (!invalidContributionIds.isEmpty()) {
      throw new IllegalArgumentException("Invalid contribution IDs: " + invalidContributionIds);
    }

    var contributions = contributionPaymentRepository.saveAll(requestList.stream().map(this::toEntity).toList());
    var balanceHistories = contributions.stream().map(
        this::balanceHistory)
      .toList();

    balanceHistoryService.saveList(balanceHistories);
    return contributions;
  }

  ContributionPayment toEntity(ContributionPaymentRequest contributionPaymentRequest) {

    return ContributionPayment.builder().contribution(Contribution.builder().id(
        contributionPaymentRequest.getContributionId()).build())
      .amount(contributionPaymentRequest.getAmount())
      .paymentDate(contributionPaymentRequest.getPaymentDate())
      .user(User.builder().userId(contributionPaymentRequest.getUserId()).build())
      .build();
  }

  public BalanceHistory balanceHistory(ContributionPayment contributionPayment) {
    return BalanceHistory.builder()
      .transactionDate(contributionPayment.getPaymentDate())
      .user(contributionPayment.getUser())
      .description("Contribution Payment")
      .amount(contributionPayment.getAmount())
      .transactionType(TransactionType.ADMINISTRATIVE)
      .build();
  }
}
