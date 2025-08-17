package com.ajustadoati.sc.adapter.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalResponse(
        Integer userId,
        BigDecimal withdrawalAmount,
        BigDecimal previousBalance,
        BigDecimal newBalance,
        String description,
        LocalDateTime transactionDate,
        String message
) {
    
    public static WithdrawalResponse success(Integer userId, BigDecimal amount, BigDecimal previousBalance, 
                                           BigDecimal newBalance, String description) {
        return new WithdrawalResponse(
                userId,
                amount,
                previousBalance,
                newBalance,
                description,
                LocalDateTime.now(),
                "Withdrawal processed successfully"
        );
    }
}
