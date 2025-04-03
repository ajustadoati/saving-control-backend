package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.response.SummaryDto;
import com.ajustadoati.sc.application.service.UserAccountSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
public class UserSummaryController {

  @Autowired
  private UserAccountSummaryService userAccountSummaryService;


  @GetMapping("/{userId}")
  public ResponseEntity<SummaryDto> getBalance(@PathVariable Integer userId) {
    var summary = userAccountSummaryService.findByUserId(userId);

    return ResponseEntity.ok(
      new SummaryDto(summary.getUser().getUserId(), summary.getInitialBalance(),
        summary.getCurrentBalance(),
        summary.getLastUpdated()));
  }

  @PostMapping
  public ResponseEntity<SummaryDto> saveBalance(@RequestBody SummaryDto summaryDto) {
    var summary = userAccountSummaryService.save(summaryDto);

    return ResponseEntity.ok(
      new SummaryDto(summary.getUser().getUserId(), summary.getInitialBalance(),
        summary.getCurrentBalance(), summary.getLastUpdated()));
  }


}
