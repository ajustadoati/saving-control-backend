package com.ajustadoati.sc.domain;

import com.ajustadoati.sc.domain.enums.LubricantMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lubricant_stock_movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LubricantStockMovement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "lubricant_stock_movement_id")
  private Integer lubricantStockMovementId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "lubricant_product_id", nullable = false)
  private LubricantProduct product;

  @Column(name = "movement_date", nullable = false)
  private LocalDate movementDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "movement_type", nullable = false, length = 20)
  private LubricantMovementType movementType;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "previous_stock", nullable = false)
  private Integer previousStock;

  @Column(name = "new_stock", nullable = false)
  private Integer newStock;

  @Column(length = 300)
  private String notes;
}
