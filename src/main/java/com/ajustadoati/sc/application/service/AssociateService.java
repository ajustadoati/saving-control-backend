package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.exception.AssociationAlreadyExistsException;
import com.ajustadoati.sc.adapter.rest.dto.request.AssociateRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.AssociateDto;
import com.ajustadoati.sc.adapter.rest.repository.AssociateRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.mapper.UserAssociateMapper;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserAssociate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssociateService {

  private final AssociateRepository associateRepository;

  private final UserRepository userRepository;

  private final UserAssociateMapper userAssociateMapper;

  public AssociateService(AssociateRepository associateRepository, UserRepository userRepository,
    UserAssociateMapper userAssociateMapper) {
    this.associateRepository = associateRepository;
    this.userRepository = userRepository;
    this.userAssociateMapper = userAssociateMapper;
  }

  public User associateUser(Integer userId, AssociateRequest request) {

    var user = userRepository.findById(userId).orElseThrow();
    var associate = userRepository.findById(request.getAssociateId()).orElseThrow();

    var members = user.getAssociates();
    var userAssociate = UserAssociate.builder().userAssociate(associate)
      .relationship(request.getRelationship())
      .build();

    if (members.stream()
      .map(UserAssociate::getUserAssociate)
      .map(User::getUserId)
      .anyMatch(request.getAssociateId()::equals)) {
      throw new AssociationAlreadyExistsException("Associate is already member");
    }

    userAssociate.setUser(user);
    associateRepository.save(userAssociate);

    return user;
  }

  public void removeAssociated(Integer userId, Integer associateId) {

    var user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    var memberToRemove = user.getAssociates().stream()
      .filter(userAssociate -> userAssociate.getUserAssociate().getUserId().equals(associateId))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("User not found"));

    associateRepository.delete(memberToRemove);

  }

  public List<AssociateDto> getAssociatesByUserId(Integer userId) {
    var user = userRepository.findById(userId).orElseThrow();
    var associates = associateRepository.findByUser(user);
    return associates.stream().map(userAssociateMapper::toDto).toList();
  }
}
