package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PaymentFileRequest {

  private String nombre;
  private String tipoPago;
  private Double monto;
  private String cedula;
  private String fecha;

}
