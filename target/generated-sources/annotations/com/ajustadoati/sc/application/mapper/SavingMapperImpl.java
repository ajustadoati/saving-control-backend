package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.SavingDto;
import com.ajustadoati.sc.domain.Saving;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-27T13:31:16+0100",
    comments = "version: 1.6.2, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class SavingMapperImpl implements SavingMapper {

    @Override
    public SavingDto toDto(Saving saving) {
        if ( saving == null ) {
            return null;
        }

        SavingDto.SavingDtoBuilder savingDto = SavingDto.builder();

        savingDto.savingId( saving.getSavingId() );
        savingDto.savingDate( saving.getSavingDate() );
        if ( saving.getAmount() != null ) {
            savingDto.amount( saving.getAmount().doubleValue() );
        }

        return savingDto.build();
    }
}
