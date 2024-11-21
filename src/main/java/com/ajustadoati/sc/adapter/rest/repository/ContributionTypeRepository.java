package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.ContributionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionTypeRepository extends JpaRepository<ContributionType, Integer> {

  boolean existsByName(String name);

}
