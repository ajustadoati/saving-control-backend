package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Integer> {
  List<Loan> findByUser_UserId(Integer userId);

  List<Loan> findByStartDate(LocalDate startDate);
}
