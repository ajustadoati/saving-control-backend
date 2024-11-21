package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContributionDto {
  private Integer id;
  private String contributionTypeName;
  private BigDecimal amount;
  private String description;
  private LocalDate contributionDate;
}