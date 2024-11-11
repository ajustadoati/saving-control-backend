
package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "collection")
public class DefaultPaymentDto {

  private Integer id;

  private String paymentName;

  private BigDecimal amount;

}
