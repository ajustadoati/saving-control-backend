package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.repository.UserAccountSummaryRepository;
import com.ajustadoati.sc.domain.UserAccountSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAccountSummaryService {

  private final UserAccountSummaryRepository repository;

  public UserAccountSummary save(UserAccountSummary summary) {
    summary.setLastUpdated(LocalDateTime.now());
    return repository.save(summary);
  }

  public Optional<UserAccountSummary> findByUserId(Integer userId) {
    return repository.findByUser_UserId(userId);
  }
}
