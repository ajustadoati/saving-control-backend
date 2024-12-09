package com.ajustadoati.sc.adapter.rest.dto.request;

import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDetail {

      private String paymentType;
      private Integer referenceId;
      private BigDecimal amount;
      private Integer userId;
}
