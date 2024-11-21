package com.ajustadoati.sc.adapter.rest.repository;


import com.ajustadoati.sc.domain.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Integer> {
  List<Contribution> findByContributionTypeId(Integer contributionTypeId);
}
