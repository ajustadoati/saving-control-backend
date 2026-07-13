package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lubricant_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LubricantProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "lubricant_product_id")
  private Integer lubricantProductId;

  @Column(nullable = false, unique = true, length = 80)
  private String code;

  @Column(nullable = false, length = 300)
  private String name;

  @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal costPrice;

  @Column(name = "sale_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal salePrice;

  @Column(nullable = false)
  private Integer stock;

  @Column(nullable = false)
  private Boolean active;
}
