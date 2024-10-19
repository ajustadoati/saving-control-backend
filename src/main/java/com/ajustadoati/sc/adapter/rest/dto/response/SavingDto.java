
package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Saving Response class
 *
 * @author rojasric
 */
@Data
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "collection")
public class SavingDto {

  private Integer savingId;

  private LocalDate savingDate;

  private Double amount;

}
