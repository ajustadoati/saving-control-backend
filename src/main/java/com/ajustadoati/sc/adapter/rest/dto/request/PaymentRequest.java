package com.ajustadoati.sc.adapter.rest.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentRequest {

    private Integer userId; // User making the payment
    private List<PaymentDetail> payments;
    private LocalDate date;

}
