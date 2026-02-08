package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Integer> {
  List<Loan> findByUser_UserId(Integer userId);

  List<Loan> findByStartDate(LocalDate startDate);

  @Query("select coalesce(sum(l.loanAmount), 0) from Loan l where l.startDate < :date")
  java.math.BigDecimal sumLoanAmountBefore(@Param("date") LocalDate date);
}
