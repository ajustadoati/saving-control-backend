package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanPaymentResponse {

  private Integer paymentId;
  private Integer loanId;
  private LocalDate paymentDate;
  private String paymentTypeName;
  private BigDecimal amount;

}
