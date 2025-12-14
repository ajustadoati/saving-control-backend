package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
  private BigDecimal loanAmount;

  @Column(name = "interest_rate", nullable = false, precision = 5)
  private BigDecimal interestRate;

  @Column(precision = 10)
  private BigDecimal loanBalance;

  private String reason;

  private LocalDate startDate;

  private LocalDate endDate;

  @ManyToOne
  @JoinColumn(name = "loan_type_id", nullable = false)
  private LoanType loanType;

}
