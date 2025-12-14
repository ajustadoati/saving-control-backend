package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanResponse {
  private Integer loanId;
  private BigDecimal loanAmount;
  private BigDecimal interestRate;
  private BigDecimal loanBalance;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private String loanTypeName;
  private String numberId;
}
