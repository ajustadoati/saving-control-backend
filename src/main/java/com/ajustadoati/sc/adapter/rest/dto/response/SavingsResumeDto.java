
package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class SavingsResumeDto {
  private BigDecimal total;

  private Long totalUsers;

}
