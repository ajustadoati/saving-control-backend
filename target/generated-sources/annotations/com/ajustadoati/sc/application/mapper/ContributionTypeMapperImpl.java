package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.ContributionTypeDto;
import com.ajustadoati.sc.domain.ContributionType;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-27T13:31:16+0100",
    comments = "version: 1.6.2, compiler: javac, environment: Java 23.0.2 (Homebrew)"
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
