package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.Supply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyRepository extends JpaRepository<Supply, Integer> {
  List<Supply> findByUser_UserId(Integer userId);
}
