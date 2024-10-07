package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "loan_id")
  private Integer loanId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "loan_amount", nullable = false, precision = 10)
  private Double loanAmount;

  @Column(name = "interest_rate", nullable = false, precision = 5)
  private Double interestRate;

  @Column(name = "loan_balance", nullable = false, precision = 10)
  private Double loanBalance;

  @Column(name = "issued_at", nullable = false)
  private LocalDate issuedAt;
}
