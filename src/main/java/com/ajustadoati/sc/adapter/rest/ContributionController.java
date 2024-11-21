package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.ContributionDto;
import com.ajustadoati.sc.application.service.ContributionService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
public class ContributionController {

  private final ContributionService contributionService;

  @PostMapping
  public ResponseEntity<ContributionDto> saveContribution(@RequestBody ContributionRequest request) {
    return ResponseEntity.ok(contributionService.saveContribution(request));
  }

  @GetMapping
  public ResponseEntity<List<ContributionDto>> getAllContributions() {
    return ResponseEntity.ok(contributionService.getAllContributions());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ContributionDto> getContributionById(@PathVariable Integer id) {
    return ResponseEntity.ok(contributionService.getContributionById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteContribution(@PathVariable Integer id) {
    contributionService.deleteContribution(id);
    return ResponseEntity.noContent().build();
  }
}