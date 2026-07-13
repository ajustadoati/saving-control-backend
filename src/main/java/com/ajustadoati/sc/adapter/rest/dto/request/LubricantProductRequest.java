package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LubricantProductRequest {
  private String code;
  private String name;
  private BigDecimal costPrice;
  private BigDecimal salePrice;
  private Integer initialStock;
}
