package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentResponse {
  private Integer userId;
  private BigDecimal totalPaid;
  private List<PaymentStatus> paymentStatuses;

  @Data
  public static class PaymentStatus {
    private String paymentType;
    private Integer referenceId;
    private BigDecimal amount;
    private String status; // SUCCESS, FAILURE
    private String message; // Error or success message
  }
}
