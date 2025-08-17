package com.ajustadoati.sc.adapter.rest.dto.response;

import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentReversalResponse(
        Integer userId,
        LocalDate paymentDate,
        LocalDateTime reversalDateTime,
        BigDecimal totalReversedAmount,
        int reversedPaymentsCount,
        List<ReversedPaymentDetail> reversedPayments,
        String reason,
        String message
) {
    
    public record ReversedPaymentDetail(
            TipoPagoEnum paymentType,
            BigDecimal amount,
            String description
    ) {}
    
    public static PaymentReversalResponse success(Integer userId, LocalDate paymentDate, 
                                                 BigDecimal totalAmount, List<ReversedPaymentDetail> details, 
                                                 String reason) {
        return new PaymentReversalResponse(
                userId,
                paymentDate,
                LocalDateTime.now(),
                totalAmount,
                details.size(),
                details,
                reason,
                "Payments successfully reversed"
        );
    }
    
    public static PaymentReversalResponse noPaymentsFound(Integer userId, LocalDate paymentDate, String reason) {
        return new PaymentReversalResponse(
                userId,
                paymentDate,
                LocalDateTime.now(),
                BigDecimal.ZERO,
                0,
                List.of(),
                reason,
                "No payments found for the specified date"
        );
    }
}
