package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "funds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Funds {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "fund_id")
  private Integer fundId;

  @Column(name = "initial_capital", nullable = false, precision = 12)
  private BigDecimal initialCapital;

  @Column(name = "current_balance", nullable = false, precision = 12)
  private BigDecimal currentBalance;

  @Column(name = "last_updated", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDate lastUpdated;

  @Override
  public String toString() {
    return "Funds{" +
      "fundId=" + fundId +
      ", initialCapital=" + initialCapital +
      ", currentBalance=" + currentBalance +
      ", lastUpdated=" + lastUpdated +
      '}';
  }
}

