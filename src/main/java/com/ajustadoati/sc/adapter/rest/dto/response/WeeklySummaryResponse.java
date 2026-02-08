package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklySummaryResponse {
  private LocalDate date;
  private Double ahorro;
  private Double intereses1;
  private Double capital1;
  private Double capital2;
  private Double capitalExt;
  private Double ingresos;
  private Double egresos;
  private Double totalDia;
  private Double saldoAnterior;
  private Double saldoFinal;
  private Double interestIncome;
  private Double savingsIncome;
  private Double loanPrincipalIncome;
  private Double loansOutflow;
  private String message;
}
