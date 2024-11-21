package com.ajustadoati.sc.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "contribution_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="contribution_type_id")
  private Integer id;

  @Column(name="contribution_type_name")
  private String name;

}
