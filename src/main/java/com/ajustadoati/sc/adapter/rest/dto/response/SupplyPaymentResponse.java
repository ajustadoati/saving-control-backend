package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SupplyPaymentResponse {
  private Integer supplyPaymentId;
  private Integer supplyId;
  private LocalDate paymentDate;
  private BigDecimal amount;
}
