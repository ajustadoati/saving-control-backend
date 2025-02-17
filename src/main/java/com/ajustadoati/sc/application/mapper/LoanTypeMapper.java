package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.LoanTypeDto;
import com.ajustadoati.sc.domain.LoanType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanTypeMapper {

  LoanTypeDto toDto(LoanType loanType);

}
