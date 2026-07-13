package com.ajustadoati.sc.adapter.rest.dto.response;

import com.ajustadoati.sc.domain.enums.LubricantOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LubricantOrderResponse {
  private Integer orderId;
  private Integer userId;
  private String userName;
  private String numberId;
  private LocalDate orderDate;
  private BigDecimal totalAmount;
  private BigDecimal balance;
  private BigDecimal weeklyInstallment;
  private LubricantOrderStatus status;
  private Integer supplyId;
  private List<LubricantOrderItemResponse> items;
}
