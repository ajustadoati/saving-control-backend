package com.ajustadoati.sc.adapter.rest.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Class
 *
 * @author rojasric
 */
@RestControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

  private static final String VALIDATION_REQUEST_FAILED_TITLE = "User Request validation failed";

  public static final String NOT_FOUND_TITLE = "Not Found";

  /**
   * Handles a case when any mass action field is null
   *
   * @param ex      {@link MethodArgumentNotValidException}
   * @param request input request
   * @return {@link HttpEntity} containing standard body in case of errors.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public HttpEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    var errors = new ArrayList<String>();
    ex
        .getBindingResult()
        .getAllErrors()
        .forEach((ObjectError error) -> {
          var errorMessage = error.getDefaultMessage();
          if (error instanceof FieldError) {
            var fieldName = ((FieldError) error).getField();
            errors.add(fieldName + " " + errorMessage);
          } else {
            errors.add(error.getObjectName() + " " + errorMessage);
          }
        });
    var formattedErrors = String.join(", ", errors);

    return handle(ex, request, HttpStatus.BAD_REQUEST, VALIDATION_REQUEST_FAILED_TITLE,
        formattedErrors);
  }

  /**
   * Handles a case when mass action is not found
   *
   * @param ex      {@link JpaObjectRetrievalFailureException}
   * @param request input request
   * @return {@link HttpEntity} containing standard body in case of errors.
   */
  @ExceptionHandler(JpaObjectRetrievalFailureException.class)
  public HttpEntity<ErrorResponse> handleMassActionNotFoundException(
      JpaObjectRetrievalFailureException ex, HttpServletRequest request) {

    return handle(ex, request, HttpStatus.NOT_FOUND, NOT_FOUND_TITLE, ex
        .getCause()
        .getMessage());
  }

  /**
   * Handles a case when property for sort in pageable request is unknown
   *
   * @param ex      {@link IllegalArgumentException}
   * @param request input request
   * @return {@link HttpEntity} containing standard body in case of errors.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public HttpEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
      HttpServletRequest request) {

    return handle(ex, request, HttpStatus.BAD_REQUEST, VALIDATION_REQUEST_FAILED_TITLE,
        ex.getMessage());
  }

  /**
   * Handles a case when property for sort in pageable request is unknown
   *
   * @param ex      {@link AssociationAlreadyExistsException}
   * @param request input request
   * @return {@link HttpEntity} containing standard body in case of errors.
   */
  @ExceptionHandler(AssociationAlreadyExistsException.class)
  public HttpEntity<ErrorResponse> handleAssociationAlreadyExistsException(AssociationAlreadyExistsException ex,
    HttpServletRequest request) {

    return handle(ex, request, HttpStatus.BAD_REQUEST, VALIDATION_REQUEST_FAILED_TITLE,
      ex.getMessage());
  }

  private HttpEntity<ErrorResponse> handle(Throwable e, HttpServletRequest request,
      HttpStatus httpStatus, String title, String detail) {
    log.error(e.getMessage());

    var problem = new ErrorResponse(title, detail);
    problem.setStatus(httpStatus.value());
    problem.setInstance(request.getRequestURI());

    return new ResponseEntity<>(problem, overrideContentType(), httpStatus);
  }

  private static HttpHeaders overrideContentType() {
    var httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    return httpHeaders;
  }

}
