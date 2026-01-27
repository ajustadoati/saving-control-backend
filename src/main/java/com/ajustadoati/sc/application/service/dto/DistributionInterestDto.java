package com.ajustadoati.sc.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionInterestDto {

    private Integer userId;
    private String name;
    private BigDecimal totalBalance;
    private BigDecimal distributedAmount;
    private BigDecimal interest;
    private LocalDate date;
}
