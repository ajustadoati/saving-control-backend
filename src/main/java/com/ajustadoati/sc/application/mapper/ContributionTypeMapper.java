package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.ContributionTypeDto;
import com.ajustadoati.sc.domain.ContributionType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContributionTypeMapper {

  ContributionTypeDto toDto(ContributionType contributionType);

}
