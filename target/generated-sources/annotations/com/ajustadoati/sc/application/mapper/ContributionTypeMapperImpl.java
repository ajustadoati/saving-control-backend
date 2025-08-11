package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.ContributionTypeDto;
import com.ajustadoati.sc.domain.ContributionType;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-11T17:42:01+0200",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class ContributionTypeMapperImpl implements ContributionTypeMapper {

    @Override
    public ContributionTypeDto toDto(ContributionType contributionType) {
        if ( contributionType == null ) {
            return null;
        }

        ContributionTypeDto.ContributionTypeDtoBuilder contributionTypeDto = ContributionTypeDto.builder();

        contributionTypeDto.id( contributionType.getId() );
        contributionTypeDto.name( contributionType.getName() );

        return contributionTypeDto.build();
    }
}
