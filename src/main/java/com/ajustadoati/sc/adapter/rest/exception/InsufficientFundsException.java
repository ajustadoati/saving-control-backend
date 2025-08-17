package com.ajustadoati.sc.adapter.rest.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    
    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;
    
    public InsufficientFundsException(String message, BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(message);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }
    
    public InsufficientFundsException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        this(String.format("Insufficient funds. Available: %s, Requested: %s", 
                availableBalance, requestedAmount), 
                availableBalance, requestedAmount);
    }
    
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
