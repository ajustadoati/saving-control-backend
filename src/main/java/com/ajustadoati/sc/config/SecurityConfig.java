package com.ajustadoati.sc.config;

import com.ajustadoati.sc.adapter.rest.filter.JwtTokenFilter;
import com.ajustadoati.sc.application.service.CustomUserDetailsService;
import com.ajustadoati.sc.application.service.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
public class SecurityConfig{

  private final CustomUserDetailsService userDetailsService;

  private final JwtTokenProvider jwtTokenProvider;

  public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> corsConfigurationSource())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/users/**").hasRole("ADMIN")
            .requestMatchers("/api/rasa/**").permitAll()
            .requestMatchers("/api/products/**").hasRole("ADMIN")
            .requestMatchers("/api/loanTypes/**").hasRole("ADMIN")
            .requestMatchers("/api/file/**").hasRole("ADMIN")
            .requestMatchers("/api/payments/**").permitAll()
            .requestMatchers("/api/contributionTypes/**").hasRole("ADMIN")
            .requestMatchers("/api/contributions/**").hasRole("ADMIN")
            .requestMatchers("/api/funds/**").hasRole("ADMIN")
            .requestMatchers("/api/sharing-funds/**").hasRole("ADMIN")
            .requestMatchers("/api/summary/**").hasRole("ADMIN")
            .requestMatchers("/api/distributions/**").hasRole("ADMIN")
            .requestMatchers("/api/balance-history/**").hasRole("ADMIN")
            .requestMatchers("/api/ai/**").hasRole("ADMIN")
            .requestMatchers("/api/associates").hasAnyRole("USER")
            .anyRequest().authenticated()
        )
        .addFilterBefore(new JwtTokenFilter(jwtTokenProvider, userDetailsService), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder());
    return authenticationManagerBuilder.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("http://localhost:4200");
    config.addAllowedOrigin("https://calch.ajustadoati.com");
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

}

