package com.ajustadoati.sc.application.service;
import com.ajustadoati.sc.config.properties.JwtProviderProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

  private SecretKey secretKey;

  @Autowired
  private JwtProviderProperties jwtProviderProperties;

  @PostConstruct
  public void init() {
    this.secretKey = Keys.hmacShaKeyFor(jwtProviderProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
  }

  public String createToken(String username, List<String> roles) {
    return Jwts.builder()
        .setSubject(username)
        .claim("roles", roles.stream().map(role -> "ROLE_" + role).collect(Collectors.toList()))
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000))
        .signWith(secretKey)
        .compact();
  }


  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }


  public List<SimpleGrantedAuthority> getRolesFromToken(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    List<String> roles = claims.get("roles", List.class);
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(role))
        .collect(Collectors.toList());
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    return claimsResolver.apply(claims);
  }

}

