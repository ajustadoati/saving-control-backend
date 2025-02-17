package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.repository.LoanTypeRepository;
import com.ajustadoati.sc.domain.LoanType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanTypeService {

  private final LoanTypeRepository loanTypeRepository;

  public LoanTypeService(LoanTypeRepository loanTypeRepository) {
    this.loanTypeRepository = loanTypeRepository;
  }

  public List<LoanType> getAllTypes(){
    return loanTypeRepository.findAll();
  }

}
