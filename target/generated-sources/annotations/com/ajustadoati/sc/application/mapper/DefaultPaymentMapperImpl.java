package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.DefaultPaymentDto;
import com.ajustadoati.sc.domain.DefaultPayment;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-02T22:18:04+0200",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.5 (Amazon.com Inc.)"
)
@Component
public class DefaultPaymentMapperImpl implements DefaultPaymentMapper {

    @Override
    public DefaultPaymentDto toDto(DefaultPayment defaultPayment) {
        if ( defaultPayment == null ) {
            return null;
        }

        DefaultPaymentDto.DefaultPaymentDtoBuilder defaultPaymentDto = DefaultPaymentDto.builder();

        defaultPaymentDto.id( defaultPayment.getId() );
        defaultPaymentDto.paymentName( defaultPayment.getPaymentName() );
        defaultPaymentDto.amount( defaultPayment.getAmount() );

        return defaultPaymentDto.build();
    }
}
