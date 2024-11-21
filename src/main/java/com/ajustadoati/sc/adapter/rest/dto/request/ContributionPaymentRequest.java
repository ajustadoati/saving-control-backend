package com.ajustadoati.sc.adapter.rest.dto.request;

import com.ajustadoati.sc.domain.Contribution;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ContributionPaymentRequest {

  private Integer userId;

  private Integer contributionId;

  private BigDecimal amount;

  private LocalDate paymentDate;
}
