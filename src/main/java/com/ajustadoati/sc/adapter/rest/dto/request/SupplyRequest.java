package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SupplyRequest {
  private Integer userId;
  private String supplyName;
  private BigDecimal supplyAmount;
  private LocalDate supplyDate;
}

