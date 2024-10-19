package com.ajustadoati.sc.adapter.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "collection")
public class UserDto{
  private Integer id;
  private String firstName;
  private String lastName; 
  private String numberId; 
  private String mobileNumber;
  private String email;
  private String company;
  private List<AssociateDto> associates;
  private List<DefaultPaymentDto> defaultPayments;

}
