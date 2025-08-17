package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.ContributionPayment;
import com.ajustadoati.sc.domain.Saving;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ContributionPaymentRepository extends JpaRepository<ContributionPayment, Integer> {

  @Query("SELECT e FROM ContributionPayment e WHERE (:paymentDate is null OR e.paymentDate = :paymentDate)")
  Page<ContributionPayment> findAllByPaymentDate(LocalDate paymentDate, Pageable pageable);

  List<ContributionPayment> findByUserAndPaymentDateBetween(User user, LocalDate startDate,
    LocalDate endDate);
    
  List<ContributionPayment> findByUserAndPaymentDate(User user, LocalDate paymentDate);

}
