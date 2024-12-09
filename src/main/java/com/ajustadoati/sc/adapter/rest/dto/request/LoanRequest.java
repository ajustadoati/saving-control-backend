package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {
  private Integer userId;
  private BigDecimal loanAmount;
  private BigDecimal interestRate;
  private BigDecimal loanBalance;
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer loanTypeId;

}

