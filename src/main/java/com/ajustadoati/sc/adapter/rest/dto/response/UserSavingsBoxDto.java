package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
public class UserSavingsBoxDto {

  private Integer userId;

  private Integer boxCount;

  private BigDecimal boxValue;

  private LocalDate updatedAt;
}
