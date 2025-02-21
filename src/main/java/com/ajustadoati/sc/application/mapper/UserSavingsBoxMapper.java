package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.UserSavingsBoxDto;
import com.ajustadoati.sc.domain.UserSavingsBox;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSavingsBoxMapper {

  UserSavingsBoxDto toDto(UserSavingsBox userSavingsBox);

}
