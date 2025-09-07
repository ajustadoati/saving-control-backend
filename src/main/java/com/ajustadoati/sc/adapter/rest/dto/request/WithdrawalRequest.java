package com.ajustadoati.sc.adapter.rest.dto.request;

import com.ajustadoati.sc.domain.enums.WithdrawalType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawalRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than 0")
        @DecimalMin(value = "0.01", message = "Minimum withdrawal amount is 0.01")
        BigDecimal amount,
        
        String description,
        
        @NotNull(message = "Withdrawal type is required")
        WithdrawalType withdrawalType
) {
}
