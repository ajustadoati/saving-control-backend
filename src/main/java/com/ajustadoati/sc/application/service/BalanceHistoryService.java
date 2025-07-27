package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.adapter.rest.repository.BalanceHistoryRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.BalanceHistory;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BalanceHistoryService {

  private final BalanceHistoryRepository repository;
  private final UserRepository userRepository;

  public BalanceHistory save(BalanceHistoryDto history) {

    var balance = new BalanceHistory();
    if (Objects.isNull(history.transactionDate())) {
        balance.setTransactionDate(LocalDate.now());
    } else {
        balance.setTransactionDate(history.transactionDate());
    }

    var user = new User();
    user.setUserId(history.userId());
    balance.setUser(user);
    balance.setDescription(history.description());
    balance.setAmount(history.amount());
    balance.setTransactionType(history.transactionType());
    return repository.save(balance);
  }

  public List<BalanceHistory> findAll() {
    return repository.findAll();
  }

  public List<BalanceHistoryDto> findAllByUser(Integer userId) {
    var user = userRepository.findById(userId).orElseThrow();
    return repository.findAllByUser(user).stream().map(
      balance -> new BalanceHistoryDto(balance.getHistoryId(), balance.getUser().getUserId(),
        balance.getTransactionDate(), balance.getTransactionType(), balance.getAmount(),
        balance.getDescription())).toList();
  }

  public List<BalanceHistoryDto> findAllByUserAndDate(Integer userId, LocalDate date) {
    var user = userRepository.findById(userId).orElseThrow();
    return repository.findAllByUserAndTransactionDate(user, date).stream().map(
      balance -> new BalanceHistoryDto(balance.getHistoryId(), balance.getUser().getUserId(),
        balance.getTransactionDate(), balance.getTransactionType(), balance.getAmount(),
        balance.getDescription())).toList();
  }

  public void saveList(List<BalanceHistory> balanceHistories) {
    repository.saveAll(balanceHistories);
  }


}
