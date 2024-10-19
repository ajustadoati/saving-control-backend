
package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "default_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultPayment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private String paymentName;

  private BigDecimal amount;
}

