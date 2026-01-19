package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.application.service.dto.PagoDto;
import com.ajustadoati.sc.domain.Pago;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-27T13:31:16+0100",
    comments = "version: 1.6.2, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class PagoMapperImpl implements PagoMapper {

    @Override
    public Pago toEntity(PagoDto pagoDto) {
        if ( pagoDto == null ) {
            return null;
        }

        Pago.PagoBuilder pago = Pago.builder();

        pago.cedula( pagoDto.getCedula() );
        pago.tipoPago( pagoDto.getTipoPago() );
        pago.monto( BigDecimal.valueOf( pagoDto.getMonto() ) );
        if ( pagoDto.getFecha() != null ) {
            pago.fecha( LocalDate.parse( pagoDto.getFecha() ) );
        }

        return pago.build();
    }

    @Override
    public PagoDto toDto(Pago pago) {
        if ( pago == null ) {
            return null;
        }

        PagoDto.PagoDtoBuilder pagoDto = PagoDto.builder();

        pagoDto.cedula( pago.getCedula() );
        pagoDto.tipoPago( pago.getTipoPago() );
        if ( pago.getMonto() != null ) {
            pagoDto.monto( pago.getMonto().doubleValue() );
        }
        if ( pago.getFecha() != null ) {
            pagoDto.fecha( DateTimeFormatter.ISO_LOCAL_DATE.format( pago.getFecha() ) );
        }

        return pagoDto.build();
    }
}
