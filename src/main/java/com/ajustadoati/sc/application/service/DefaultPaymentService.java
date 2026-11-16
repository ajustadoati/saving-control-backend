package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.DefaultPaymentRequest;
import com.ajustadoati.sc.adapter.rest.repository.DefaultPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.DefaultPayment;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Default payment service class
 *
 * @author rojasric
 */
@Service
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
    var name = defaultPaymentRequest.getDefaultPaymentName();
    if (name != null && name.trim().toLowerCase(Locale.ROOT).startsWith("suministro")) {
      throw new IllegalArgumentException("Los suministros ya no se configuran como pagos por defecto");
    }
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

  public void deleteDefaultPayment(Integer userId, Integer id){
    var payment = defaultPaymentRepository.findByIdAndUser_UserId(id, userId)
        .orElseThrow(() -> new IllegalArgumentException("El pago por defecto no pertenece al socio"));
    defaultPaymentRepository.delete(payment);
  }

}
