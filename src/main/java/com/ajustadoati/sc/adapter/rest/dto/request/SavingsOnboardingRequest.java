package com.ajustadoati.sc.adapter.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsOnboardingRequest {

  @NotNull
  private Boolean joinSavingsBox;

  @NotNull
  @Min(0)
  private BigDecimal initialBalance;

  @NotNull
  @Min(0)
  private BigDecimal interestEarned;

  @NotNull
  @Min(1)
  private Integer boxCount;

  @NotNull
  @Min(0)
  private BigDecimal boxValue;

  @NotNull
  private LocalDate effectiveDate;
}
