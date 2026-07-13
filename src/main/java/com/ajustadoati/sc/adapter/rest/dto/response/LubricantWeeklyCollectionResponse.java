package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LubricantWeeklyCollectionResponse {
  private Integer orderId;
  private Integer userId;
  private String userName;
  private String numberId;
  private LocalDate orderDate;
  private BigDecimal weeklyInstallment;
  private BigDecimal balance;
  private BigDecimal suggestedPayment;
}
