package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.SupplyPaymentResponse;
import com.ajustadoati.sc.domain.SupplyPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplyPaymentMapper {

  @Mapping(target = "supplyId", source = "supply.supplyId")
  SupplyPaymentResponse toResponse(SupplyPayment supplyPayment);

}
