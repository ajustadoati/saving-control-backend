package com.ajustadoati.sc.domain;

import com.ajustadoati.sc.domain.enums.LubricantOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lubricant_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LubricantOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "lubricant_order_id")
  private Integer lubricantOrderId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "order_date", nullable = false)
  private LocalDate orderDate;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal balance;

  @Column(name = "weekly_installment", nullable = false, precision = 12, scale = 2)
  private BigDecimal weeklyInstallment;

  @Column(name = "installment_count", nullable = false)
  private Integer installmentCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LubricantOrderStatus status;

  @Column(name = "supply_id", unique = true)
  private Integer supplyId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LubricantOrderItem> items;
}
