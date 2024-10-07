package com.ajustadoati.sc.adapter.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
  @GetMapping("/public")
  public String publicEndpoint() {

    return "Public Endpoint";
  }

  @PostMapping("/user")
  public String userEndpoint() {
    return "User Endpoint";
  }

  @PostMapping("/admin")
  public String adminEndpoint() {

    return "Admin Endpoint";
  }
}
