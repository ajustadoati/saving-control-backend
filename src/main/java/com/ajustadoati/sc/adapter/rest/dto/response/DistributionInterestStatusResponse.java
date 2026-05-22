package com.ajustadoati.sc.adapter.rest.dto.response;

import com.ajustadoati.sc.application.service.dto.DistributionInterestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionInterestStatusResponse {
  private LocalDate date;
  private boolean distributed;
  private LocalDate previousPendingDate;
  private List<DistributionInterestDto> distributions;
}
