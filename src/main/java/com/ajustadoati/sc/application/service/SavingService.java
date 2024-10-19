package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.Saving;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Saving service class
 *
 * @author rojasric
 */
@Service
@Slf4j
public class SavingService {

  private final UserRepository userRepository;

  private final SavingRepository savingRepository;

  public SavingService(UserRepository userRepository, SavingRepository savingRepository) {
    this.userRepository = userRepository;
    this.savingRepository = savingRepository;
  }

  public Saving addSaving(Integer userId, SavingRequest savingRequest) {
    var user = userRepository
        .findById(userId)
        .orElseThrow();

    var saving = Saving
        .builder()
        .savingDate(savingRequest.getSavingDate())
        .user(user)
        .amount(savingRequest.getAmount())
        .build();

    return savingRepository.save(saving);
  }

  public Page<Saving> getAllByUserId(Integer userId, Pageable pageable) {
    var user = userRepository
        .findById(userId)
        .orElseThrow();
    return savingRepository.getAllByUser(user, pageable);
  }

  public Page<Saving> getAllSavingsByDate(LocalDate date, Pageable pageable) {

    return savingRepository.findAllBySavingDate(date, pageable);
  }

}
