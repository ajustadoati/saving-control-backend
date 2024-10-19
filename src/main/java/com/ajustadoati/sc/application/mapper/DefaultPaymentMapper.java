
package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.DefaultPaymentDto;
import com.ajustadoati.sc.domain.DefaultPayment;
import org.mapstruct.Mapper;

/**
 * User mapper class
 *
 * @author rojasric
 */

@Mapper(componentModel = "spring")
public interface DefaultPaymentMapper {

  DefaultPaymentDto toDto(DefaultPayment defaultPayment);

}
