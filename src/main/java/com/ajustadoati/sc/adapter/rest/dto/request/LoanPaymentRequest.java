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
public class LoanPaymentRequest {
  private Integer loanId;
  private LocalDate paymentDate;
  private Integer paymentTypeId; // 1: Principal, 2: Interest
  private BigDecimal amount;
}
