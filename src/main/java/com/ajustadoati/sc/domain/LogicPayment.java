package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "logic_payment")
@Getter @Setter @NoArgsConstructor
public class LogicPayment {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, length = 36)
  private String requestId;
  @Column(nullable = false)
  private LocalDate paymentDate;
  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;
  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal balanceAfter;
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @Column(nullable = false)
  private String createdBy;
}
