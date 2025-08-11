package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.repository.SharingFundsRepository;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.SharingFunds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class SharingFundsService {

  private final SharingFundsRepository sharingFundsRepository;


  public void saveFunds(BigDecimal amount, FundsType fundsType) {

    log.info("Saving funds: {}", amount);
    var currentFunds = sharingFundsRepository.findAll().getFirst();
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
    sharingFundsRepository.saveAndFlush(currentFunds);
  }

  public SharingFunds getFunds(){
    return sharingFundsRepository.findAll()
        .getFirst();
  }


}
