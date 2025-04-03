package com.ajustadoati.sc.adapter.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SummaryDto(Integer userId, BigDecimal initialBalance, BigDecimal currentBalance,
                         LocalDate lastUpdated) {

}
