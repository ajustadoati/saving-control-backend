package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.UserSavingsBoxRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.UserSavingsBoxDto;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.service.UserSavingsBoxService;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserSavingsBox;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/savings-box")
@RequiredArgsConstructor
public class UserSavingsBoxController {

  private final UserSavingsBoxService userSavingsBoxService;
  private final UserRepository userRepository;

  @PostMapping
  public ResponseEntity<UserSavingsBoxDto> createOrUpdateSavingsBox(
    @PathVariable Integer userId,
    @RequestBody UserSavingsBoxRequest request) {

    return new ResponseEntity<>(userSavingsBoxService.save(request), HttpStatus.CREATED);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<List<UserSavingsBoxDto>> findByUser(@PathVariable Integer userId) {

    return new ResponseEntity<>(userSavingsBoxService.getAllUserSavingsBox(userId), HttpStatus.OK);
  }
}
