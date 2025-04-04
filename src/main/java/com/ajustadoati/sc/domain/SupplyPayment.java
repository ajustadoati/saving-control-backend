package com.ajustadoati.sc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supply_payment")
@Data
public class SupplyPayment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer supplyPaymentId;

  @ManyToOne
  @JoinColumn(name = "supply_id", nullable = false)
  private Supply supply;

  @Column(nullable = false)
  private LocalDate paymentDate;

  @Column(nullable = false)
  private BigDecimal amount;
}

