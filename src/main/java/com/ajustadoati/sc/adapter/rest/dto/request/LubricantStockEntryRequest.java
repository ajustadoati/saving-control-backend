package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LubricantStockEntryRequest {
  private Integer quantity;
  private LocalDate movementDate;
  private String notes;
}
