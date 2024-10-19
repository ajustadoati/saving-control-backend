package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AssociateDto {
  private Integer id;
  private String firstName;
  private String lastName;
  private String numberId;
  private String relationship;

}
