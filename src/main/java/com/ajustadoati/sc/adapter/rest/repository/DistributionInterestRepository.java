package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.DistributionInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DistributionInterestRepository extends JpaRepository<DistributionInterest, Integer> {
  boolean existsByUser_UserIdAndDistributionDate(Integer userId, LocalDate distributionDate);

  List<DistributionInterest> findByDistributionDate(LocalDate distributionDate);
}
