package com.ajustadoati.sc.adapter.rest.assemblers;

import com.ajustadoati.sc.adapter.rest.DefaultPaymentController;
import com.ajustadoati.sc.adapter.rest.dto.response.DefaultPaymentDto;
import com.ajustadoati.sc.application.mapper.DefaultPaymentMapper;
import com.ajustadoati.sc.domain.DefaultPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@Component
public class DefaultPaymentModelAssembler
    extends RepresentationModelAssemblerSupport<DefaultPayment, EntityModel<DefaultPaymentDto>> {

  private final DefaultPaymentMapper defaultPaymentMapper;

  public DefaultPaymentModelAssembler(DefaultPaymentMapper defaultPaymentMapper) {
    super(DefaultPaymentController.class,
        (Class<EntityModel<DefaultPaymentDto>>) (Class<?>) EntityModel.class);
    this.defaultPaymentMapper = defaultPaymentMapper;
  }

  @Override
  public EntityModel<DefaultPaymentDto> toModel(DefaultPayment defaultPayment) {
    var defaultPaymentDto = defaultPaymentMapper.toDto(defaultPayment);

    return EntityModel.of(defaultPaymentDto, WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(DefaultPaymentController.class)
            .getAllByUserId(defaultPayment
                .getUser()
                .getUserId(), null))
        .withRel("user-default-payments"), WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(DefaultPaymentController.class)
            .saveDefaultPayment(defaultPayment.getId(), null))
        .withRel("add-default-payment"));
  }

  public PagedModel<EntityModel<DefaultPaymentDto>> toPagedModel(
      Page<DefaultPayment> defaultPaymentsPage,
      PagedResourcesAssembler<DefaultPaymentDto> assembler) {

    var paymentsDtoPage = defaultPaymentsPage.map(defaultPaymentMapper::toDto);
    var pagedModel = assembler.toModel(paymentsDtoPage);


    pagedModel.add(WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(DefaultPaymentController.class)
            .getAllByUserId(null, defaultPaymentsPage.getPageable()))
        .withSelfRel());

    if (defaultPaymentsPage.hasNext()) {
      pagedModel.add(WebMvcLinkBuilder
          .linkTo(WebMvcLinkBuilder
              .methodOn(DefaultPaymentController.class)
              .getAllByUserId(null, defaultPaymentsPage.nextPageable()))
          .withRel("next"));
    }

    if (defaultPaymentsPage.hasPrevious()) {
      pagedModel.add(WebMvcLinkBuilder
          .linkTo(WebMvcLinkBuilder
              .methodOn(DefaultPaymentController.class)
              .getAllByUserId(null, defaultPaymentsPage.previousPageable()))
          .withRel("previous"));
    }

    return pagedModel;
  }
}

