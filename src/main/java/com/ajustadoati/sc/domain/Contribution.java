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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contribution {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contribution_id")
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "contribution_type_id", nullable = false)
  private ContributionType contributionType;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private LocalDate contributionDate;

}