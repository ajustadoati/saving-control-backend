package com.ajustadoati.sc.adapter.rest.dto.response;


import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DailyResponse {

  private LocalDate fecha;
  private Map<String, Map<TipoPagoEnum, Double>> pagos;
  private Map<TipoPagoEnum, Double> totalPorTipoPago;
  private Double montoTotal;
  private String mensaje;
}
