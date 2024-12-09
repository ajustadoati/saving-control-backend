package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.SupplyPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyPaymentRepository extends JpaRepository<SupplyPayment, Integer> {
  List<SupplyPayment> findBySupply_SupplyId(Integer supplyId);
}
