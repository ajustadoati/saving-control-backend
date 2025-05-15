
package com.ajustadoati.sc.adapter.rest.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(
    ignoreUnknown = true
)
public class ErrorResponse {

  private String type;

  private String title;

  private Integer status;

  private String detail;

  private String instance;

  private List<ErrorResponse> errors;

  public ErrorResponse(String title, String detail) {
    this.title = title;
    this.detail = detail;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public void setErrors(List<ErrorResponse> errors) {
    this.errors = errors;
  }

  public String getType() {
    return this.type;
  }

  public String getTitle() {
    return this.title;
  }

  public Integer getStatus() {
    return this.status;
  }

  public String getDetail() {
    return this.detail;
  }

  public String getInstance() {
    return this.instance;
  }

  public List<ErrorResponse> getErrors() {
    return this.errors;
  }

  public ErrorResponse() {
  }
}

