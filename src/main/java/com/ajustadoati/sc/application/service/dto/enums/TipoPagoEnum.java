package com.ajustadoati.sc.application.service.dto.enums;

import java.util.Arrays;

public enum TipoPagoEnum {
  AHORRO("AHORRO", "AHORRO"),
  COMPARTIR("COMPARTIR", "COMPARTIR"),
  PRESTAMOS("PRESTAMOS", "PRESTAMOS"),
  PRESTAMO_EXTERNO("PRESTAMOS2", "PRESTAMO2"),
  ADMINISTRATIVO("ADMINISTRATIVO", "ADMINISTRATIVO"),
  ABONO_CAPITAL("Abono capital", "PRESTAMOS"),
  ABONO_INTERES("Abono intereses", "PRESTAMOS"),
  SUMINISTROS("Suministros", "SUMINISTROS"),
  OTROS("Otros", "OTROS");

  private final String description;
  private final String category;

  TipoPagoEnum(String description, String category) {
    this.description = description;
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public static TipoPagoEnum fromDescription(String description) {
    for (TipoPagoEnum tipo : values()) {
      if (tipo.getDescription().equalsIgnoreCase(description)) {
        return tipo;
      }
    }
    throw new IllegalArgumentException("Invalid Payment Type: " + description);
  }

  public static TipoPagoEnum[] fromCategory(String category) {
    return Arrays.stream(values()).filter(tipo -> tipo.getCategory().equalsIgnoreCase(category))
      .toArray(TipoPagoEnum[]::new);

  }

}