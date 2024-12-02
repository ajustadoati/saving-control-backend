package com.ajustadoati.sc.adapter.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  private Integer id;

  @NotNull
  private String firstName;

  @NotNull
  private String lastName;

  @NotNull
  private String numberId;

  private String mobileNumber;

  private String company;

  @Email
  private String email;

  @NotEmpty
  private Set<String> roles;

}
