package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.DefaultPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.repository.DefaultPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.DefaultPayment;
import com.ajustadoati.sc.domain.Saving;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Default payment service class
 *
 * @author rojasric
 */
@Service
@Slf4j
public class DefaultPaymentService {

  private final DefaultPaymentRepository defaultPaymentRepository;

  private final UserRepository userRepository;

  public DefaultPaymentService(DefaultPaymentRepository defaultPaymentRepository,
      UserRepository userRepository) {
    this.defaultPaymentRepository = defaultPaymentRepository;
    this.userRepository = userRepository;
  }

  public DefaultPayment addDefaultPayment(Integer userId,
      DefaultPaymentRequest defaultPaymentRequest) {
    var user = userRepository
        .findById(userId)
        .orElseThrow();

    var defaultPayment = DefaultPayment
        .builder()
        .user(user)
        .paymentName(defaultPaymentRequest.getDefaultPaymentName())
        .amount(defaultPaymentRequest.getAmount())
        .build();

    return defaultPaymentRepository.save(defaultPayment);
  }

  public Page<DefaultPayment> getAllByUserId(Integer userId, Pageable pageable) {
    var user = userRepository
        .findById(userId)
        .orElseThrow();
    return defaultPaymentRepository.getAllByUser(user, pageable);
  }

}
