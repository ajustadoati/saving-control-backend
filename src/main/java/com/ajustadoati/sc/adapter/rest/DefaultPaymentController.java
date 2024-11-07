package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.assemblers.DefaultPaymentModelAssembler;
import com.ajustadoati.sc.adapter.rest.dto.request.DefaultPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.DefaultPaymentDto;
import com.ajustadoati.sc.application.service.DefaultPaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default Payment controller class
 *
 * @author rojasric
 */
@RestController
@RequestMapping("/api/users")
public class DefaultPaymentController {

  private final DefaultPaymentService defaultPaymentService;

  private final DefaultPaymentModelAssembler defaultPaymentModelAssembler;

  private final PagedResourcesAssembler<DefaultPaymentDto> pagedResourcesAssembler;

  public DefaultPaymentController(DefaultPaymentService defaultPaymentService,
      DefaultPaymentModelAssembler defaultPaymentModelAssembler,
      PagedResourcesAssembler<DefaultPaymentDto> pagedResourcesAssembler) {
    this.defaultPaymentService = defaultPaymentService;
    this.defaultPaymentModelAssembler = defaultPaymentModelAssembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @PostMapping(value = "/{userId}/defaultPayments")
  public ResponseEntity<EntityModel<DefaultPaymentDto>> saveDefaultPayment(
      @PathVariable Integer userId, @RequestBody DefaultPaymentRequest defaultPaymentRequest) {

    var saving = defaultPaymentService.addDefaultPayment(userId, defaultPaymentRequest);

    return new ResponseEntity(defaultPaymentModelAssembler.toModel(saving), HttpStatus.CREATED);
  }

  @GetMapping(value = "/{userId}/defaultPayments")
  public ResponseEntity<CollectionModel<EntityModel<DefaultPaymentDto>>> getAllByUserId(
      @PathVariable Integer userId, Pageable pageable) {
    var defaults = defaultPaymentService.getAllByUserId(userId, pageable);

    PagedModel<EntityModel<DefaultPaymentDto>> pagedModel =
        defaultPaymentModelAssembler.toPagedModel(defaults, pagedResourcesAssembler);

    return ResponseEntity.ok(pagedModel);
  }

  @DeleteMapping(value = "/{userId}/defaultPayments/{id}")
  public void delete(@PathVariable Integer userId, @PathVariable Integer id){

    defaultPaymentService.deleteDefaultPayment(userId, id);
  }

}
