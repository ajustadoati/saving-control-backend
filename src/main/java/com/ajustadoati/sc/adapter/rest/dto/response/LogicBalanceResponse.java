package com.ajustadoati.sc.adapter.rest.dto.response;

import java.math.BigDecimal;

public record LogicBalanceResponse(BigDecimal initialBalance, BigDecimal currentBalance) {}
