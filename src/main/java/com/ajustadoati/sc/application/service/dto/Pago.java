package com.ajustadoati.sc.application.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pago {
  private String cedula;
  private String tipoPago;
  private double monto;
  private String fecha;
}
