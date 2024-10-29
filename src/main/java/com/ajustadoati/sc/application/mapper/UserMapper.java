
package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.UserDto;
import com.ajustadoati.sc.domain.Saving;
import com.ajustadoati.sc.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

/**
 * User mapper class
 *
 * @author rojasric
 */

@Mapper(uses = {UserAssociateMapper.class, DefaultPaymentMapper.class, SavingMapper.class}, componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "id", source = "userId")
  @Mapping(target = "totalSavings", source = "savings", qualifiedByName = "calculateTotalSavings")
  UserDto toDto(User user);

  @Named("calculateTotalSavings")
  default BigDecimal calculateTotalSavings(List<Saving> savings) {
    if (savings == null || savings.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return savings.stream()
        .map(Saving::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}
