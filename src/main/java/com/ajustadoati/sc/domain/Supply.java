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
@Table(name = "supply")
@Data
public class Supply {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer supplyId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private BigDecimal supplyAmount;

  @Column(nullable = false)
  private BigDecimal supplyBalance;

  @Column(nullable = false)
  private LocalDate supplyDate;

  private String supplyName;

}

