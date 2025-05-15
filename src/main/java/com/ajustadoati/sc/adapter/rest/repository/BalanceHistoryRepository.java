package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.BalanceHistory;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Integer> {

  public List<BalanceHistory> findAllByUser(User user);

  public List<BalanceHistory> findAllByUserAndTransactionDate(User user, LocalDate transactionDate);

}
