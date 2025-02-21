package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_savings_box")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSavingsBox {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "box_count", nullable = false)
  private Integer boxCount;

  @Column(name = "box_value", nullable = false, precision = 10, scale = 2)
  private BigDecimal boxValue;

  @Column(name = "updated_at", nullable = false)
  private LocalDate updatedAt;
}
