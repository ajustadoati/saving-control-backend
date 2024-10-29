package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.CreateUserRequest;
import com.ajustadoati.sc.adapter.rest.repository.RoleRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.Saving;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User service class
 *
 * @author rojasric
 */
@Service
public class UserService {

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final SavingRepository savingRepository;

  public UserService(UserRepository userRepository, RoleRepository roleRepository,
      SavingRepository savingRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.savingRepository = savingRepository;
  }

  public User createUser(CreateUserRequest createUserRequest) {

    var user = new User();
    user.setFirstName(createUserRequest.getFirstName());
    user.setLastName(createUserRequest.getLastName());

    user.setEmail(createUserRequest.getEmail());
    user.setCreatedAt(Instant.now());

    var roles = roleRepository.findByRoleNames(createUserRequest.getRoles());
    user.setRoles(roles);
    var userCreated = userRepository.save(user);

    return userCreated;
  }

  public Page<User> getUsersWithSavingsByDateRange(LocalDate startDate, LocalDate endDate,
      Pageable pageable) {
    Page<User> users = userRepository.findAll(pageable);

    List<User> userList = users
        .getContent()
        .stream()
        .map(user -> {
          List<Saving> filteredSavings =
              savingRepository.findByUserAndSavingDateBetween(user, startDate,
                  endDate);
          user.setSavings(filteredSavings);

          return User
              .builder()
              .userId(user.getUserId())
              .firstName(user.getFirstName())
              .lastName(user.getLastName())
              .numberId(user.getNumberId())
              .email(user.getEmail())
              .mobileNumber(user.getMobileNumber())
              .company(user.getCompany())
              .savings(filteredSavings)
              .build();
        })
        .collect(Collectors.toList());

    return new PageImpl<>(userList, pageable, users.getTotalElements());
  }

  public User getUserById(Integer id) {
    return userRepository
        .findById(id)
        .orElseThrow();
  }

  public User getUserByNumberId(String numberId) {
    return userRepository
        .findByNumberId(numberId)
        .orElseThrow();
  }

  public Page<User> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

}
