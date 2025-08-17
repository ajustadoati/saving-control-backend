package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.OtherPayment;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OtherPaymentRepository extends JpaRepository<OtherPayment, Long> {

  List<OtherPayment> findAllByUser(User user);
  
  List<OtherPayment> findByUserAndPaymentDate(User user, LocalDate paymentDate);
}
