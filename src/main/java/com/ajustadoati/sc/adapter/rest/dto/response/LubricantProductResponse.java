package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LubricantProductResponse {
  private Integer id;
  private String code;
  private String name;
  private BigDecimal costPrice;
  private BigDecimal salePrice;
  private Integer stock;
  private Boolean active;
}
