package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "logic_balance")
@Getter @Setter @NoArgsConstructor
public class LogicBalance {
  @Id
  private Long id;
  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal initialBalance;
  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal currentBalance;
}
