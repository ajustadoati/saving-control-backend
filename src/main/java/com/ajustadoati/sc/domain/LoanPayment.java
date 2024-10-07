package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "loan_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPayment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Integer paymentId;

  @ManyToOne
  @JoinColumn(name = "loan_id", nullable = false)
  private Loan loan;

  @Column(name = "payment_date", nullable = false)
  private LocalDate paymentDate;

  @Column(name = "amount", nullable = false, precision = 10)
  private Double amount;
}
