package com.ajustadoati.sc.adapter.rest.assemblers;

/**
 * User assembler class
 *
 * @author rojasric
 */

import com.ajustadoati.sc.adapter.rest.UserController;
import com.ajustadoati.sc.adapter.rest.dto.response.UserDto;
import com.ajustadoati.sc.application.mapper.UserMapper;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class UserModelAssembler
    extends RepresentationModelAssemblerSupport<User, EntityModel<UserDto>> {
    private final UserMapper userMapper;

    // Inyectamos el mapper a través del constructor
    public UserModelAssembler(UserMapper userMapper) {
      super(UserController.class, (Class<EntityModel<UserDto>>) (Class<?>) EntityModel.class);
      this.userMapper = userMapper;
    }

    @Override
    public EntityModel<UserDto> toModel(User user) {

      UserDto userDto = userMapper.toDto(user);

      return EntityModel.of(userDto,
          WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserById(userDto.getId())).withSelfRel(),
          WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers(null)).withRel("users"));
    }

    public PagedModel<EntityModel<UserDto>> toPagedModel(Page<User> usersPage, PagedResourcesAssembler<UserDto> assembler) {

      Page<UserDto> usersDtoPage = usersPage.map(userMapper::toDto);

      PagedModel<EntityModel<UserDto>> pagedModel = assembler.toModel(usersDtoPage);

      pagedModel.add(
          WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers(usersPage.getPageable()))
              .withSelfRel()
      );

      if (usersPage.hasNext()) {
        pagedModel.add(
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                    .getAllUsers(usersPage.nextPageable()))
                .withRel("next")
        );
      }

      if (usersPage.hasPrevious()) {
        pagedModel.add(
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                    .getAllUsers(usersPage.previousPageable()))
                .withRel("previous")
        );
      }

      return pagedModel;
    }
}
