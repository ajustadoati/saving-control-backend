
package com.ajustadoati.sc.adapter.rest.dto.response;

import com.ajustadoati.sc.domain.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class DefaultPaymentDto {
  private String paymentName;

  private BigDecimal amount;
}
