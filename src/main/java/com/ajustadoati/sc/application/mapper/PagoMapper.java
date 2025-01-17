package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.application.service.dto.PagoDto;
import com.ajustadoati.sc.domain.Pago;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PagoMapper {

  Pago toEntity(PagoDto pagoDto);

  PagoDto toDto(Pago pago);



}
