package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.response.TokenDto;
import com.ajustadoati.sc.adapter.rest.request.LoginRequest;
import com.ajustadoati.sc.application.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<TokenDto> authenticateUser(@RequestBody LoginRequest loginRequest) {
    log.info("Executing post");
    String token = authService.login(loginRequest.username(), loginRequest.password());
    return ResponseEntity.ok(new TokenDto(token));
  }
}
