package it.gov.pagopa.apiconfig.testingsupport.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum AppError {
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
      "Something was wrong"),
  DANGEROUS_QUERY(HttpStatus.FORBIDDEN, "Dangerous query detected.",
          "Dangerous query detected, the query was not executed."),
  FILE_EMPTY(HttpStatus.BAD_REQUEST, "File empty.",
          "File must be a valid sql file"),
  WRONG_QUERY_GRAMMAR(HttpStatus.BAD_REQUEST, "Something went wrong.",
      "Something went wrong with the query, wrong grammar or invalid table.");

  public final HttpStatus httpStatus;
  public final String title;
  public final String details;


  AppError(HttpStatus httpStatus, String title, String details) {
    this.httpStatus = httpStatus;
    this.title = title;
    this.details = details;
  }
}


