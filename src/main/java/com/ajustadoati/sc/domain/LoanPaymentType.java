package com.ajustadoati.sc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "loan_payment_type")
@Data
public class LoanPaymentType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer loanPaymentTypeId;

  @Column(nullable = false)
  private String loanPaymentTypeName;
}
