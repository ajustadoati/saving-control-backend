package com.ajustadoati.sc.adapter.rest.dto.response;

import com.ajustadoati.sc.domain.enums.LubricantMovementType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LubricantStockMovementResponse {
  private Integer id;
  private LocalDate movementDate;
  private LubricantMovementType movementType;
  private Integer quantity;
  private Integer previousStock;
  private Integer newStock;
  private String notes;
}
