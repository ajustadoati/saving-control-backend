package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.ContributionDto;
import com.ajustadoati.sc.adapter.rest.repository.ContributionRepository;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.application.mapper.ContributionMapper;
import com.ajustadoati.sc.domain.Contribution;
import com.ajustadoati.sc.domain.ContributionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionService {

  private final ContributionRepository contributionRepository;
  private final ContributionTypeRepository contributionTypeRepository;
  private final ContributionMapper contributionMapper;


  public ContributionDto saveContribution(ContributionRequest request) {
    ContributionType contributionType = contributionTypeRepository.findById(request.getContributionTypeId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid Contribution Type ID"));


    var contribution = Contribution.builder()
      .contributionType(contributionType)
      .amount(request.getAmount())
      .description(request.getName())
      .contributionDate(LocalDate.now()).build();

    Contribution savedContribution = contributionRepository.save(contribution);

    return contributionMapper.toDto(savedContribution);
  }

  public List<ContributionDto> getAllContributions() {
    return contributionRepository.findAll()
      .stream()
      .map(contributionMapper::toDto)
      .toList();
  }

  public ContributionDto getContributionById(Integer id) {
    var contribution = contributionRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Contribution not found"));
    return contributionMapper.toDto(contribution);
  }

  public void deleteContribution(Integer id) {
    var contribution = contributionRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Contribution not found"));
    contributionRepository.delete(contribution);
  }

  public boolean existsById(Integer contributionId) {
    return contributionRepository.existsById(contributionId);
  }
}