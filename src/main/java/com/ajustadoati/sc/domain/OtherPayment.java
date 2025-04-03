package com.ajustadoati.sc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "other_payment")
@Data
public class OtherPayment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="other_payment_id")
  private Integer id;

  @Column(nullable = false)
  private String name;

  @Column(name = "payment_date", nullable = false)
  private LocalDate paymentDate;

  @Column(name = "amount", nullable = false, precision = 10)
  private BigDecimal amount;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
