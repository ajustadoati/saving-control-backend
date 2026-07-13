package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lubricant_order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LubricantOrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "lubricant_order_item_id")
  private Integer lubricantOrderItemId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "lubricant_order_id", nullable = false)
  private LubricantOrder order;

  @ManyToOne(optional = false)
  @JoinColumn(name = "lubricant_product_id", nullable = false)
  private LubricantProduct product;

  @Column(name = "product_code", nullable = false, length = 80)
  private String productCode;

  @Column(name = "product_name", nullable = false, length = 300)
  private String productName;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal lineTotal;
}
