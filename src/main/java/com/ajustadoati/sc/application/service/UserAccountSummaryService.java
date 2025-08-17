package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.SummaryDto;
import com.ajustadoati.sc.adapter.rest.exception.BalanceAlreadyExistException;
import com.ajustadoati.sc.adapter.rest.exception.InsufficientFundsException;
import com.ajustadoati.sc.adapter.rest.repository.UserAccountSummaryRepository;
import com.ajustadoati.sc.domain.BalanceHistory;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserAccountSummary;
import com.ajustadoati.sc.domain.enums.TransactionType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountSummaryService {

  private final UserAccountSummaryRepository repository;
  private final BalanceHistoryService balanceHistoryService;

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
      .orElseThrow(() -> new EntityNotFoundException("User has not balance "+ userId));
  }

  public List<UserAccountSummary> getAll(){
      return repository.findAll();
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

  public UserAccountSummary withdrawFunds(Integer userId, BigDecimal amount, String description) {
    log.info("Processing withdrawal for userId: {}, amount: {}", userId, amount);
    
    var userAccount = repository.findByUser_UserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("User account not found for userId: " + userId));
    
    var currentBalance = userAccount.getCurrentBalance();
    
    // Validar que hay fondos suficientes
    if (currentBalance.compareTo(amount) < 0) {
      log.warn("Insufficient funds for userId: {}. Available: {}, Requested: {}", 
               userId, currentBalance, amount);
      throw new InsufficientFundsException(currentBalance, amount);
    }
    
    // Realizar el retiro (restar del balance actual)
    var newBalance = currentBalance.subtract(amount);
    userAccount.setCurrentBalance(newBalance);
    userAccount.setLastUpdated(LocalDate.now());
    
    var updatedAccount = repository.save(userAccount);
    log.info("Withdrawal successful for userId: {}. New balance: {}", userId, newBalance);
    
    // Registrar la transacción en el historial
    recordWithdrawalTransaction(userId, amount, description);
    
    return updatedAccount;
  }

  private void recordWithdrawalTransaction(Integer userId, BigDecimal amount, String description) {
    log.info("Recording withdrawal transaction for userId: {}, amount: {}", userId, amount);
    
    var balanceHistory = BalanceHistory.builder()
        .user(User.builder().userId(userId).build())
        .transactionDate(LocalDate.now())
        .transactionType(TransactionType.WITHDRAWAL)
        .amount(amount)
        .description(description != null ? description : "Withdrawal")
        .build();
    
    balanceHistoryService.saveList(List.of(balanceHistory));
    log.info("Withdrawal transaction recorded successfully for userId: {}", userId);
  }
}
