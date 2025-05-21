package com.ajustadoati.sc.adapter.rest.dto.request;

import com.ajustadoati.sc.application.service.dto.DistributionInterestDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DistributionInterestRequest {

    private List<DistributionInterestDto> distributionInterestList;
    private LocalDate date;

}
