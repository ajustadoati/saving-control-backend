package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.SupplyPayment;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SupplyPaymentRepository extends JpaRepository<SupplyPayment, Integer> {
  List<SupplyPayment> findBySupply_SupplyId(Integer supplyId);
  
  @Query("SELECT sp FROM SupplyPayment sp WHERE sp.supply.user = :user AND sp.paymentDate = :paymentDate")
  List<SupplyPayment> findByUserAndPaymentDate(@Param("user") User user, @Param("paymentDate") LocalDate paymentDate);
}
