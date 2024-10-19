
package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.SavingDto;
import com.ajustadoati.sc.domain.Saving;
import org.mapstruct.Mapper;

/**
 * User mapper class
 *
 * @author rojasric
 */

@Mapper(componentModel = "spring")
public interface SavingMapper {

  SavingDto toDto(Saving saving);

}
