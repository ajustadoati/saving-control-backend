
package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.UserDto;
import com.ajustadoati.sc.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * User mapper class
 *
 * @author rojasric
 */

@Mapper(uses = {UserAssociateMapper.class, DefaultPaymentMapper.class}, componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "id", source = "userId")
  UserDto toDto(User user);

}
