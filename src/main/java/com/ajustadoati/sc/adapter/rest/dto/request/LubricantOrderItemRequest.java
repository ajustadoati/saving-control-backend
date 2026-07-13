package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LubricantOrderItemRequest {
  private Integer productId;
  private Integer quantity;
  private BigDecimal unitPrice;
}
