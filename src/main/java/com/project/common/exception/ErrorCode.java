package com.project.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // Common
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "Invalid input"),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "Resource not found"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),

  // Auth
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "Access denied");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
