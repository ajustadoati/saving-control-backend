package com.ajustadoati.sc.application.service;


import com.ajustadoati.sc.adapter.rest.dto.request.UserSavingsBoxRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.UserSavingsBoxDto;
import com.ajustadoati.sc.adapter.rest.repository.UserSavingsBoxRepository;
import com.ajustadoati.sc.application.mapper.UserSavingsBoxMapper;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserSavingsBox;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSavingsBoxService {

  private final UserSavingsBoxRepository repository;

  private final UserService userService;

  private final UserSavingsBoxMapper userSavingsBoxMapper;


  public List<UserSavingsBox> findAll() {
    return repository.findAll();
  }

  public UserSavingsBoxDto save(UserSavingsBoxRequest request) {
    User user = userService.getUserById(request.getUserId());

    var boxData = UserSavingsBox.builder().boxCount(request.getBoxCount())
      .boxValue(request.getBoxValue()).user(user).updatedAt(LocalDate.now()).build();

    return Optional.of(repository.save(boxData)).map(userSavingsBoxMapper::toDto).orElseThrow();
  }


  public List<UserSavingsBoxDto> getAllUserSavingsBox(Integer userId) {

    return repository.findByUser(User.builder().userId(userId).build()).stream()
      .map(userSavingsBoxMapper::toDto).toList();
  }
}
