package com.ajustadoati.sc.adapter.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;


public record FundsDto(BigDecimal initialCapital, BigDecimal currentBalance, LocalDate lastUpdated){
};
