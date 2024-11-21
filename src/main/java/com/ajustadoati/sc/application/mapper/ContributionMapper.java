package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.ContributionDto;
import com.ajustadoati.sc.adapter.rest.dto.response.ContributionTypeDto;
import com.ajustadoati.sc.domain.Contribution;
import com.ajustadoati.sc.domain.ContributionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContributionMapper {

  @Mapping(target = "contributionTypeName", source = "contributionType.name")
  ContributionDto toDto(Contribution contribution);

}
