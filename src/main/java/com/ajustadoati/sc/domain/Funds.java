package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

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
  private Double initialCapital;

  @Column(name = "current_balance", nullable = false, precision = 12)
  private Double currentBalance;

  @Column(name = "last_updated", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Timestamp lastUpdated;
}

