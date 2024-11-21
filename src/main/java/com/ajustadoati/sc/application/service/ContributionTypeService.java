package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionTypeRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.ContributionTypeDto;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.application.mapper.ContributionTypeMapper;
import com.ajustadoati.sc.domain.ContributionType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ContributionTypeService {


  private final ContributionTypeRepository repository;
  private final ContributionTypeMapper mapper;

  public ContributionTypeService(ContributionTypeRepository repository,
    ContributionTypeMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public ContributionTypeDto createContributionType(ContributionTypeRequest request) {
    if (repository.existsByName(request.getName())) {
      throw new IllegalArgumentException("There is already a contribution type with this name.");
    }

    ContributionType contributionType = ContributionType.builder()
      .name(request.getName())
      .build();

    ContributionType savedContributionType = repository.save(contributionType);
    return mapper.toDto(savedContributionType);
  }

  @Transactional()
  public List<ContributionTypeDto> getAllContributionTypes() {
    return repository.findAll()
      .stream()
      .map(mapper::toDto)
      .toList();
  }

  @Transactional()
  public ContributionTypeDto getContributionTypeById(Integer id) {
    ContributionType contributionType = repository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Not Found."));
    return mapper.toDto(contributionType);
  }

  @Transactional
  public void deleteContributionType(Integer id) {
    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("Not Found.");
    }
    repository.deleteById(id);
  }

}
