package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.response.LoanTypeDto;
import com.ajustadoati.sc.application.mapper.LoanTypeMapper;
import com.ajustadoati.sc.application.service.LoanTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loan-types")
public class LoanTypeController {

  @Autowired
  private LoanTypeService loanTypeService;

  @Autowired
  private LoanTypeMapper loanTypeMapper;

  @GetMapping
  public ResponseEntity<List<LoanTypeDto>> getAllProducts() {
    return new ResponseEntity<>(
      loanTypeService.getAllTypes().stream().map(loanTypeMapper::toDto).toList(), HttpStatus.OK);
  }

}
