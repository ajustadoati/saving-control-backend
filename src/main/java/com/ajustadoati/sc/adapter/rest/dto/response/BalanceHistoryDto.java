package com.ajustadoati.sc.adapter.rest.dto.response;

import com.ajustadoati.sc.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BalanceHistoryDto(
  Integer historyId,
  Integer userId,
  LocalDate transactionDate,
  TransactionType transactionType,
  BigDecimal amount,
  String description
) {}
