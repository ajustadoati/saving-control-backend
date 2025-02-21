package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SupplyResponse {
  private Integer supplyId;
  private String supplyName;
  private BigDecimal supplyAmount;
  private BigDecimal supplyBalance;
  private LocalDate supplyDate;
}

