
package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class DefaultPaymentDto {

  private Integer id;

  private String paymentName;

  private BigDecimal amount;
}
