package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.repository.FundsRepository;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.Funds;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class FundsService {

  private final FundsRepository fundsRepository;

  public FundsService(FundsRepository fundsRepository) {
    this.fundsRepository = fundsRepository;
  }

  public void saveFunds(BigDecimal amount, FundsType fundsType) {

    log.info("Saving funds: {}", amount);
    var currentFunds = fundsRepository.findById(1L).orElseThrow();
    log.info("Current funds: {}", currentFunds.getCurrentBalance());
    switch (fundsType) {
      case ADD:
        currentFunds.setCurrentBalance(currentFunds.getCurrentBalance().add(amount));
        break;
      case SUBTRACT:
        currentFunds.setCurrentBalance(currentFunds.getCurrentBalance().subtract(amount));
        break;
      default:
        throw new IllegalArgumentException("Invalid funds type");
    }
    log.info("Current funds: {}", currentFunds.getCurrentBalance());
    fundsRepository.saveAndFlush(currentFunds);
  }

  public Funds getFunds(){
    return fundsRepository.findById(1L).orElseThrow();
  }


}
