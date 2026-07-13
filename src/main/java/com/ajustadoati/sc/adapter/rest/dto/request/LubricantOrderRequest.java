package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class LubricantOrderRequest {
  private Integer userId;
  private LocalDate orderDate;
  private BigDecimal weeklyInstallment;
  private List<LubricantOrderItemRequest> items;
}
