
package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.AssociateDto;
import com.ajustadoati.sc.domain.UserAssociate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * User mapper class
 *
 * @author rojasric
 */

@Mapper(componentModel = "spring")
public interface UserAssociateMapper {

  @Mapping(target = "id", source = "userAssociate.userId")  // Cambia a userAssociate directamente
  @Mapping(target = "firstName", source = "userAssociate.firstName")
  @Mapping(target = "lastName", source = "userAssociate.lastName")
  @Mapping(target = "numberId", source = "userAssociate.numberId")
  @Mapping(target = "relationship", source = "relationship")
  AssociateDto toDto(UserAssociate user);

}
