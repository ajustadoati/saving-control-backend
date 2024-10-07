package com.ajustadoati.sc.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    // Crear el token de autenticación con las credenciales
    var authToken = new UsernamePasswordAuthenticationToken(username, password);

    // Autenticar al usuario
    Authentication authentication = authenticationManager.authenticate(authToken);

    // Cargar los detalles del usuario
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    // Generar el token JWT
    return jwtTokenProvider.createToken(userDetails.getUsername(),
        userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList());
  }
}
