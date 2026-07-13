package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.LubricantStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LubricantStockMovementRepository extends JpaRepository<LubricantStockMovement, Integer> {
  List<LubricantStockMovement> findByProduct_LubricantProductIdOrderByMovementDateDesc(Integer lubricantProductId);
}
