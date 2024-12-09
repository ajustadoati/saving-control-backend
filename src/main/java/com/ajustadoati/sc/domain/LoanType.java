package com.ajustadoati.sc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "loan_type")
@Data
public class LoanType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer loanTypeId;

  @Column(nullable = false)
  private String loanTypeName;
}
