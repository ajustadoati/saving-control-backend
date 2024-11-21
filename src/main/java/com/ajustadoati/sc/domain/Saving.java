package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "saving")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Saving {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "saving_id")
  private Integer savingId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "saving_date")
  private LocalDate savingDate;

  @Column(name = "amount", precision = 10)
  private BigDecimal amount;
}
