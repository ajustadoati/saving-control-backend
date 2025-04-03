package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.SavingsResumeDto;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.BalanceHistory;
import com.ajustadoati.sc.domain.Saving;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Saving service class
 *
 * @author rojasric
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SavingService {

  private final UserRepository userRepository;

  private final SavingRepository savingRepository;

  private final BalanceHistoryService balanceHistoryService;

  private final UserAccountSummaryService userAccountSummaryService;

  private final FundsService fundsService;

  public Saving addSaving(Integer userId, SavingRequest savingRequest) {
    var user = userRepository
        .findById(userId)
        .orElseThrow();

    var saving = Saving
        .builder()
        .savingDate(savingRequest.getSavingDate())
        .user(user)
        .amount(savingRequest.getAmount())
        .build();

    return savingRepository.save(saving);
  }

  @Transactional
  public List<Saving> addSavingSet(Integer userId, List<SavingRequest> savingRequests) {
    var user = userRepository
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User Not Found: " + userId));

    var savings = savingRequests
        .stream()
        .map(savingRequest -> {
          User paymentUser;

          if (savingRequest.getAssociateId() != null) {
            paymentUser = userRepository
                .findById(savingRequest.getAssociateId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Associated Not Found: " + savingRequest.getAssociateId()));
          } else {
            paymentUser = user;
          }

          return Saving
              .builder()
              .savingDate(savingRequest.getSavingDate())
              .user(paymentUser)
              .amount(savingRequest.getAmount())
              .build();
        })
        .collect(Collectors.toList());
    var balanceHistories = savings
        .stream()
        .map(this::balanceHistory)
        .toList();
    balanceHistoryService.saveList(balanceHistories);
    var savedSavings = savingRepository.saveAll(savings);

    savedSavings.forEach(saving -> {
      userAccountSummaryService.updateBalance(saving.getUser().getUserId(), saving.getAmount());
      fundsService.saveFunds(saving.getAmount(), FundsType.ADD);
    });

    return savedSavings;
  }

  public Page<Saving> getAllByUserId(Integer userId, Pageable pageable) {
    var user = userRepository
        .findById(userId)
        .orElseThrow();
    return savingRepository.getAllByUser(user, pageable);
  }

  public SavingsResumeDto getSavingResume() {
    return SavingsResumeDto
        .builder()
        .totalUsers(userRepository.count())
        .total(savingRepository.getTotalAmount())
        .build();
  }

  public BalanceHistory balanceHistory(Saving saving) {
    var balanceHistory = new BalanceHistory();
    balanceHistory.setUser(saving.getUser());
    balanceHistory.setTransactionType(TransactionType.SAVING);
    balanceHistory.setAmount(saving.getAmount());
    balanceHistory.setTransactionDate(saving.getSavingDate());
    balanceHistory.setDescription("Saving");
    return balanceHistory;
  }

}
