package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.repository.BalanceHistoryRepository;
import com.ajustadoati.sc.domain.BalanceHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceHistoryService {

  private final BalanceHistoryRepository repository;

  public BalanceHistory save(BalanceHistory history) {
    history.setCreatedAt(LocalDateTime.now());
    return repository.save(history);
  }

  public List<BalanceHistory> findAll() {
    return repository.findAll();
  }
}
