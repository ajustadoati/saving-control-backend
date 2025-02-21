package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class UserSavingsBoxRequest {

  private Integer userId;

  private Integer boxCount;

  private BigDecimal boxValue;

  private LocalDate updatedAt;

}
