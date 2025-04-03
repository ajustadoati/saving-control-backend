package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.application.service.BalanceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/balance-history")
public class BalanceHistoryController {

  private final BalanceHistoryService balanceHistoryService;

  public BalanceHistoryController(BalanceHistoryService balanceHistoryService) {
    this.balanceHistoryService = balanceHistoryService;
  }

  @GetMapping("{userId}/user")
  public ResponseEntity<List<BalanceHistoryDto>> getHistory(@PathVariable Integer userId){
    return ResponseEntity.ok(balanceHistoryService.findAllByUser(userId));
  }

  @GetMapping("{userId}/user/{date}/date")
  public ResponseEntity<List<BalanceHistoryDto>> getHistoryByDate(@PathVariable Integer userId, @PathVariable
    LocalDate date){
    return ResponseEntity.ok(balanceHistoryService.findAllByUser(userId));
  }
}
