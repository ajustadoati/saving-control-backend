package com.ajustadoati.sc.adapter.rest.dto.request.enums;

public enum PaymentTypeEnum {
  SAVING("Caja de Ahorro"),
  PARTNER_SAVING("Esposa(o)"),
  CHILDRENS_SAVING("Hijos"),
  SHARED_CONTRIBUTION("Compartir"),
  ADMINISTRATIVE("Administrativo"),
  SUPPLIES("Suministros"),
  LOAN_INTEREST_PAYMENT("Interés de préstamo"),
  LOAN_PAYMENT("Abono a préstamo"),
  LOAN_SHARING("Abono a préstamo compartir"),
  LOAN_SHARING_INTEREST("Interes a préstamo compartir"),
  LOAN_INTEREST_PAYMENT_EXTERNAL("Interés de préstamo 2"),
  LOAN_EXTERNAL_INTEREST("Interés de préstamo externo"),
  LOAN_PAYMENT_EXTERNAL("Abono a préstamo 2"),
  LOAN_EXTERNAL("Abono a préstamo externo"),
  OTHER_PAYMENTS("Otros"),
  WHEELS("Cauchos"),;

  private final String description;

  PaymentTypeEnum(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public static PaymentTypeEnum fromDescription(String description) {
    for (PaymentTypeEnum type : values()) {
      if (type.getDescription().equalsIgnoreCase(description)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid Payment Type: " + description);
  }

}
