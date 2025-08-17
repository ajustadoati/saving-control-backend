package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.LoanPayment;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Integer> {
  List<LoanPayment> findByLoan_LoanId(Integer loanId);
  
  @Query("SELECT lp FROM LoanPayment lp WHERE lp.loan.user = :user AND lp.paymentDate = :paymentDate")
  List<LoanPayment> findByUserAndPaymentDate(@Param("user") User user, @Param("paymentDate") LocalDate paymentDate);
}
