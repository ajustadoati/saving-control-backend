package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.ContributionDto;
import com.ajustadoati.sc.domain.Contribution;
import com.ajustadoati.sc.domain.ContributionType;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-02T22:18:04+0200",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.5 (Amazon.com Inc.)"
)
@Component
public class ContributionMapperImpl implements ContributionMapper {

    @Override
    public ContributionDto toDto(Contribution contribution) {
        if ( contribution == null ) {
            return null;
        }

        ContributionDto contributionDto = new ContributionDto();

        contributionDto.setContributionTypeName( contributionContributionTypeName( contribution ) );
        contributionDto.setId( contribution.getId() );
        contributionDto.setAmount( contribution.getAmount() );
        contributionDto.setDescription( contribution.getDescription() );
        contributionDto.setContributionDate( contribution.getContributionDate() );

        return contributionDto;
    }

    private String contributionContributionTypeName(Contribution contribution) {
        ContributionType contributionType = contribution.getContributionType();
        if ( contributionType == null ) {
            return null;
        }
        return contributionType.getName();
    }
}
