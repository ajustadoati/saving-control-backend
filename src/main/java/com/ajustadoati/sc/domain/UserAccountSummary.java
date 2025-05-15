package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_account_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "summary_id")
  private Integer summaryId;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "initial_balance", nullable = false, precision = 12, scale = 2)
  private BigDecimal initialBalance;

  @Column(name = "current_balance", nullable = false, precision = 12, scale = 2)
  private BigDecimal currentBalance;

  @Column(name = "last_updated", nullable = false)
  private LocalDate lastUpdated;
}
