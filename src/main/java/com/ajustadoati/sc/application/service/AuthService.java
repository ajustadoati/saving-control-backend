package com.ajustadoati.sc.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private UserDetailsService userDetailsService;

  public String login(String username, String password) {
    var authToken = new UsernamePasswordAuthenticationToken(username, password);


    Authentication authentication = authenticationManager.authenticate(authToken);


    UserDetails userDetails = userDetailsService.loadUserByUsername(username);


    return jwtTokenProvider.createToken(userDetails.getUsername(),
        userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList());
  }
}
