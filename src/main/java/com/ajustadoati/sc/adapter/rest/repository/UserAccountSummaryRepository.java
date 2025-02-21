package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.UserAccountSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountSummaryRepository extends JpaRepository<UserAccountSummary, Integer> {
  Optional<UserAccountSummary> findByUser_UserId(Integer userId);
}

