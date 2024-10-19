package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.CreateUserRequest;
import com.ajustadoati.sc.adapter.rest.repository.RoleRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.mapper.UserMapper;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * User service class
 *
 * @author rojasric
 */
@Service
public class UserService {

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final UserMapper userMapper;

  public UserService(UserRepository userRepository, RoleRepository roleRepository,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.userMapper = userMapper;
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
    return userRepository
        .findAll(pageable);
  }

}
