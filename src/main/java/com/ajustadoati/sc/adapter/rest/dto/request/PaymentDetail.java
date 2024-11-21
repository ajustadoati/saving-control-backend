package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDetail {

      private String paymentType;
      private Integer referenceId;
      private BigDecimal amount;
      private Integer userId;
}
