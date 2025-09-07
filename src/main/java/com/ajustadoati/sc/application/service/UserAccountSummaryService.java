package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.SummaryDto;
import com.ajustadoati.sc.adapter.rest.exception.BalanceAlreadyExistException;
import com.ajustadoati.sc.adapter.rest.exception.InsufficientFundsException;
import com.ajustadoati.sc.adapter.rest.repository.UserAccountSummaryRepository;
import com.ajustadoati.sc.domain.BalanceHistory;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserAccountSummary;
import com.ajustadoati.sc.domain.enums.TransactionType;
import com.ajustadoati.sc.domain.enums.WithdrawalType;
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
        .interestEarned(summary.interestEarned())
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

    public void updateInterestBalance(Integer userId, BigDecimal amount) {
        log.info("Updating interest balance for userId: {}", userId);
        var userAccount = repository.findByUser_UserId(userId);

        if (userAccount.isPresent()){
            var summary = userAccount.get();
            summary.setInterestEarned(summary.getInterestEarned().add(amount));
            repository.save(summary);
        }

    }

  public UserAccountSummary withdrawFunds(Integer userId, BigDecimal amount, String description, WithdrawalType withdrawalType) {
    log.info("Processing withdrawal for userId: {}, amount: {}, type: {}", userId, amount, withdrawalType);

    var userAccount = repository.findByUser_UserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("User account not found for userId: " + userId));

    // Obtener el balance disponible según el tipo de retiro
    BigDecimal availableBalance;
    if (withdrawalType == WithdrawalType.INTEREST) {
      availableBalance = userAccount.getInterestEarned();
    } else {
      availableBalance = userAccount.getCurrentBalance();
    }

    // Validar que hay fondos suficientes
    if (availableBalance.compareTo(amount) < 0) {
      log.warn("Insufficient funds for userId: {}. Available {}: {}, Requested: {}",
               userId, withdrawalType, availableBalance, amount);
      throw new InsufficientFundsException(availableBalance, amount);
    }

    // Realizar el retiro según el tipo
    if (withdrawalType == WithdrawalType.INTEREST) {
      // Retirar de los intereses ganados
      var newInterestBalance = userAccount.getInterestEarned().subtract(amount);
      userAccount.setInterestEarned(newInterestBalance);
      log.info("Interest withdrawal: New interest balance: {}", newInterestBalance);
    } else {
      // Retirar del balance total
      var newBalance = userAccount.getCurrentBalance().subtract(amount);
      userAccount.setCurrentBalance(newBalance);
      log.info("Total balance withdrawal: New balance: {}", newBalance);
    }

    userAccount.setLastUpdated(LocalDate.now());
    var updatedAccount = repository.save(userAccount);

    log.info("Withdrawal successful for userId: {}. Withdrawal type: {}", userId, withdrawalType);

    // Registrar la transacción en el historial
    recordWithdrawalTransaction(userId, amount, description, withdrawalType);

    return updatedAccount;
  }

  // Mantener el método original para compatibilidad
  public UserAccountSummary withdrawFunds(Integer userId, BigDecimal amount, String description) {
    return withdrawFunds(userId, amount, description, WithdrawalType.TOTAL_BALANCE);
  }

  private void recordWithdrawalTransaction(Integer userId, BigDecimal amount, String description, WithdrawalType withdrawalType) {
    log.info("Recording withdrawal transaction for userId: {}, amount: {}, type: {}", userId, amount, withdrawalType);

    String enhancedDescription = description != null ? description : "Withdrawal";
    if (withdrawalType == WithdrawalType.INTEREST) {
      enhancedDescription += " (Interest withdrawal)";
    } else {
      enhancedDescription += " (Total balance withdrawal)";
    }

    var balanceHistory = BalanceHistory.builder()
        .user(User.builder().userId(userId).build())
        .transactionDate(LocalDate.now())
        .transactionType(TransactionType.WITHDRAWAL)
        .amount(amount)
        .description(enhancedDescription)
        .build();

    balanceHistoryService.saveList(List.of(balanceHistory));
    log.info("Withdrawal transaction recorded successfully for userId: {}", userId);
  }

  // Método de compatibilidad para el método original
  private void recordWithdrawalTransaction(Integer userId, BigDecimal amount, String description) {
    recordWithdrawalTransaction(userId, amount, description, WithdrawalType.TOTAL_BALANCE);
  }
}
