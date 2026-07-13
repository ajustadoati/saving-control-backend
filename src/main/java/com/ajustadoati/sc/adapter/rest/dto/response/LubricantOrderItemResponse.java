package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LubricantOrderItemResponse {
  private Integer productId;
  private String productCode;
  private String productName;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal lineTotal;
}
