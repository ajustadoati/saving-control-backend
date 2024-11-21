package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data

public class ContributionRequest {
  private Integer contributionTypeId;
  private String name;
  private BigDecimal amount;
}
