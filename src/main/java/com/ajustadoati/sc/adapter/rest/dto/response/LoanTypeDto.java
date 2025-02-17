package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanTypeDto {

  private Integer loanTypeId;

  private String loanTypeName;

}
