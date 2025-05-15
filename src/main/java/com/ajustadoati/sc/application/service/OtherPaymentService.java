package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.repository.OtherPaymentRepository;
import com.ajustadoati.sc.domain.OtherPayment;
import com.ajustadoati.sc.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OtherPaymentService {

  private final OtherPaymentRepository repository;

  public OtherPayment save(OtherPayment otherPayment){

    return repository.save(otherPayment);
  }

  public List<OtherPayment> getAllByUser(Integer userId){
    var user = new User();
    user.setUserId(userId);
    return repository.findAllByUser(user);
  }

}
