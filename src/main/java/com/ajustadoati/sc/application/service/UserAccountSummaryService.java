package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.SummaryDto;
import com.ajustadoati.sc.adapter.rest.exception.BalanceAlreadyExistException;
import com.ajustadoati.sc.adapter.rest.repository.UserAccountSummaryRepository;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserAccountSummary;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountSummaryService {

  private final UserAccountSummaryRepository repository;

  public UserAccountSummary save(SummaryDto summary) {

    var currentSummary = repository.findByUser_UserId(summary.userId());
    if (currentSummary.isPresent()) {

      throw new BalanceAlreadyExistException("User has already a balance");
    }
    var userAccount = UserAccountSummary.builder()
      .user(User.builder().userId(summary.userId()).build())
      .initialBalance(summary.initialBalance()).currentBalance(summary.initialBalance())
      .lastUpdated(LocalDate.now()).build();
    return repository.save(userAccount);
  }

  public UserAccountSummary findByUserId(Integer userId) {
    return repository.findByUser_UserId(userId)
      .orElseThrow(() -> new EntityNotFoundException("User has not balance"));
  }

  public void updateBalance(Integer userId, BigDecimal amount) {
    log.info("Updating balance for userId: {}", userId);
    var userAccount = repository.findByUser_UserId(userId);

    if (userAccount.isPresent()){
      var summary = userAccount.get();
      summary.setCurrentBalance(summary.getCurrentBalance().add(amount));
      repository.save(summary);
    }

  }
}
