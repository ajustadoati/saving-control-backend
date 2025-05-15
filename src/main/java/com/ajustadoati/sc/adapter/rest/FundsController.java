package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.response.FundsDto;
import com.ajustadoati.sc.application.service.FundsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funds")
public class FundsController {

  @Autowired
  private FundsService fundsService;

  @GetMapping
  public ResponseEntity<FundsDto> getCurrentFunds() {
    var funds = fundsService.getFunds();
    return ResponseEntity.ok(
      new FundsDto(funds.getInitialCapital(), funds.getCurrentBalance(), funds.getLastUpdated()));
  }

}
