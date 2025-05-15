package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.CreateUserRequest;
import com.ajustadoati.sc.adapter.rest.repository.AssociateRepository;
import com.ajustadoati.sc.adapter.rest.repository.RoleRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.domain.Saving;
import com.ajustadoati.sc.domain.User;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

  private final AssociateRepository associateRepository;

  public UserService(UserRepository userRepository, RoleRepository roleRepository,
    SavingRepository savingRepository, AssociateRepository associateRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.savingRepository = savingRepository;
    this.associateRepository = associateRepository;
  }



  public User createUser(CreateUserRequest createUserRequest) {

    var user = new User();
    user.setFirstName(createUserRequest.getFirstName());
    user.setLastName(createUserRequest.getLastName());
    user.setCompany(createUserRequest.getCompany());
    user.setNumberId(createUserRequest.getNumberId());
    user.setMobileNumber(createUserRequest.getMobileNumber());
    user.setEmail(createUserRequest.getEmail());
    user.setCreatedAt(Instant.now());

    var roles = roleRepository.findByRoleNames(createUserRequest.getRoles());
    user.setRoles(roles);

    return userRepository.save(user);
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

  public void updateUserPartial(Integer id, Map<String, Object> updates) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

    updates.forEach((field, value) -> {
      BeanWrapper beanWrapper = new BeanWrapperImpl(user);
      if (beanWrapper.isWritableProperty(field)) {
        beanWrapper.setPropertyValue(field, value);
      } else {
        throw new IllegalArgumentException("Field '" + field + "' is not a valid property of User.");
      }
    });

    userRepository.save(user);
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


  public void delete(Integer id){
    User user = userRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

    if (!user.getContributionPayments().isEmpty() || !user.getSavings().isEmpty()){
      throw new RuntimeException("Is Not possible delete user");
    }

    userRepository.delete(user);

  }

}
