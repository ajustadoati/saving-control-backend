package com.ajustadoati.sc.adapter.rest.advice;

public class AssociationAlreadyExistsException extends RuntimeException {
  public AssociationAlreadyExistsException(String message) {
    super(message);
  }
}