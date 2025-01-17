package com.ajustadoati.sc.application.service.dto;

import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagoDto {
  private String cedula;
  private TipoPagoEnum tipoPago;
  private double monto;
  private String fecha;
}
