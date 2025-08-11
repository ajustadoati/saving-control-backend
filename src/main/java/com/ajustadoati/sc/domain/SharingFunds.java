package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sharing_funds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharingFunds {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "initial_capital", nullable = false, precision = 12)
  private BigDecimal initialCapital;

  @Column(name = "current_balance", nullable = false, precision = 12)
  private BigDecimal currentBalance;

  @Column(name = "last_updated", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDate lastUpdated;

}

