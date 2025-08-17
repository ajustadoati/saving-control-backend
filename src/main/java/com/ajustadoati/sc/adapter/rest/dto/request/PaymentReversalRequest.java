package com.ajustadoati.sc.adapter.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record PaymentReversalRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        
        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,
        
        String reason
) {
}
