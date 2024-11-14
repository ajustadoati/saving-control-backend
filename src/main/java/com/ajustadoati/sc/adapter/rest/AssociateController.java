package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.assemblers.UserModelAssembler;
import com.ajustadoati.sc.adapter.rest.dto.request.AssociateRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.AssociateDto;
import com.ajustadoati.sc.adapter.rest.dto.response.UserDto;
import com.ajustadoati.sc.application.service.AssociateService;
import com.ajustadoati.sc.application.service.UserService;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AssociateController {

  private final AssociateService associateService;

  private final UserModelAssembler userModelAssembler;


  public AssociateController(AssociateService associateService,
    UserModelAssembler userModelAssembler) {
    this.associateService = associateService;
    this.userModelAssembler = userModelAssembler;
  }

  @PostMapping("/{userId}/associates")
  public ResponseEntity<EntityModel<UserDto>> associateMember(
    @PathVariable Integer userId,
    @RequestBody AssociateRequest associateRequest) {
    var user = associateService.associateUser(userId, associateRequest);
    return new ResponseEntity(userModelAssembler.toModel(user), HttpStatus.OK);
  }

  @DeleteMapping("/{userId}/associates/{associateId}")
  public void removeAssociated(
    @PathVariable Integer userId,
    @PathVariable Integer associateId) {
    associateService.removeAssociated(userId, associateId);
  }

  @GetMapping("/{userId}/associates")
  public ResponseEntity<List<AssociateDto>> getAssociates(
    @PathVariable Integer userId) {
    var associates = associateService.getAssociatesByUserId(userId);
    return new ResponseEntity<>(associates, HttpStatus.OK);
  }


}
