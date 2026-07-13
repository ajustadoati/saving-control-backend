package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.LubricantProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LubricantProductRepository extends JpaRepository<LubricantProduct, Integer> {
  Optional<LubricantProduct> findByCodeIgnoreCase(String code);
}
