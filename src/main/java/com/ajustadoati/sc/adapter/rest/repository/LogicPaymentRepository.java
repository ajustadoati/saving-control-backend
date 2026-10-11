package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.LogicPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LogicPaymentRepository extends JpaRepository<LogicPayment, Long> {
  Optional<LogicPayment> findByRequestId(String requestId);
}
