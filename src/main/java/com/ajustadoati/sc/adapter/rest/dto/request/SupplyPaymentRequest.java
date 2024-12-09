package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SupplyPaymentRequest {
  private Integer supplyId;
  private LocalDate paymentDate;
  private BigDecimal amount;
}
