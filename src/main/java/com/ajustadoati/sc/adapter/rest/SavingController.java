package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.assemblers.SavingModelAssembler;
import com.ajustadoati.sc.adapter.rest.dto.response.SavingDto;
import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.application.service.SavingService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
public class SavingController {

  private final SavingService savingService;

  private final SavingModelAssembler savingModelAssembler;

  private final PagedResourcesAssembler<SavingDto> pagedResourcesAssembler;

  public SavingController(SavingService savingService, SavingModelAssembler savingModelAssembler,
      PagedResourcesAssembler<SavingDto> pagedResourcesAssembler) {
    this.savingService = savingService;
    this.savingModelAssembler = savingModelAssembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @PostMapping(value = "/{userId}/savings")
  public ResponseEntity<EntityModel<SavingDto>> addSaving(@PathVariable Integer userId,
      @RequestBody SavingRequest savingRequest) {

    var saving = savingService.addSaving(userId, savingRequest);

    return new ResponseEntity(savingModelAssembler.toModel(saving), HttpStatus.CREATED);
  }

  @GetMapping(value = "/{userId}/savings")
  public ResponseEntity<CollectionModel<EntityModel<SavingDto>>> getAllByUserId(
      @PathVariable Integer userId, Pageable pageable) {
    var savings = savingService.getAllByUserId(userId, pageable);

    PagedModel<EntityModel<SavingDto>> pagedModel =
        savingModelAssembler.toPagedModel(savings, pagedResourcesAssembler);

    return ResponseEntity.ok(pagedModel);
  }

  @GetMapping(value = "/savings")
  public ResponseEntity<CollectionModel<EntityModel<SavingDto>>> getAllSavings(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate date, Pageable pageable) {

    var savings = savingService.getAllSavingsByDate(date, pageable);

    PagedModel<EntityModel<SavingDto>> pagedModel =
        savingModelAssembler.toPagedModel(savings, pagedResourcesAssembler);

    return ResponseEntity.ok(pagedModel);
  }

}
