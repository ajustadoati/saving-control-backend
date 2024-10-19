package com.ajustadoati.sc.adapter.rest.assemblers;

import com.ajustadoati.sc.adapter.rest.SavingController;
import com.ajustadoati.sc.adapter.rest.dto.response.SavingDto;
import com.ajustadoati.sc.application.mapper.SavingMapper;
import com.ajustadoati.sc.domain.Saving;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

/**
 * Saving Model assembler class
 *
 * @author rojasric
 */
@Component
public class SavingModelAssembler
    extends RepresentationModelAssemblerSupport<Saving, EntityModel<SavingDto>> {

  private final SavingMapper savingMapper;

  public SavingModelAssembler(SavingMapper savingMapper) {
    super(SavingController.class, (Class<EntityModel<SavingDto>>) (Class<?>) EntityModel.class);
    this.savingMapper = savingMapper;
  }

  @Override
  public EntityModel<SavingDto> toModel(Saving saving) {

    var savingDto = savingMapper.toDto(saving);

    return EntityModel.of(savingDto, WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(SavingController.class)
            .addSaving(savingDto.getSavingId(), null))
        .withSelfRel(), WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(SavingController.class)
            .getAllByUserId(saving
                .getUser()
                .getUserId(), null))
        .withRel("Savings"));
  }

  public PagedModel<EntityModel<SavingDto>> toPagedModel(Page<Saving> savingsPage,
      PagedResourcesAssembler<SavingDto> assembler) {

    var savingsDtoPage = savingsPage.map(savingMapper::toDto);

    var pagedModel = assembler.toModel(savingsDtoPage);

    pagedModel.add(WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(SavingController.class)
            .getAllByUserId(null, savingsPage.getPageable()))
        .withSelfRel());

    if (savingsPage.hasNext()) {
      pagedModel.add(WebMvcLinkBuilder
          .linkTo(WebMvcLinkBuilder
              .methodOn(SavingController.class)
              .getAllByUserId(null, savingsPage.nextPageable()))
          .withRel("next"));
    }

    if (savingsPage.hasPrevious()) {
      pagedModel.add(WebMvcLinkBuilder
          .linkTo(WebMvcLinkBuilder
              .methodOn(SavingController.class)
              .getAllByUserId(null, savingsPage.previousPageable()))
          .withRel("previous"));
    }

    return pagedModel;
  }
}
