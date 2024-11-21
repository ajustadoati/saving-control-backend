package com.ajustadoati.sc.adapter.rest;


import com.ajustadoati.sc.adapter.rest.dto.request.ContributionTypeRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.ContributionTypeDto;
import com.ajustadoati.sc.application.service.ContributionTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contribution-types")
public class ContributionTypeController {

  private final ContributionTypeService service;

  public ContributionTypeController(ContributionTypeService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<ContributionTypeDto> createContributionType(@Valid @RequestBody ContributionTypeRequest request) {
    ContributionTypeDto response = service.createContributionType(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<ContributionTypeDto>> getAllContributionTypes() {
    List<ContributionTypeDto> responses = service.getAllContributionTypes();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ContributionTypeDto> getContributionTypeById(@PathVariable Integer id) {
    ContributionTypeDto response = service.getContributionTypeById(id);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteContributionType(@PathVariable Integer id) {
    service.deleteContributionType(id);
    return ResponseEntity.noContent().build();
  }

}
