package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "distribution_interest",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "distribution_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionInterest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "distribution_interest_id")
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "distribution_date", nullable = false)
  private LocalDate distributionDate;

  @Column(name = "total_balance", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalBalance;

  @Column(name = "interest_percent", nullable = false, precision = 8, scale = 4)
  private BigDecimal interestPercent;

  @Column(name = "distributed_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal distributedAmount;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
