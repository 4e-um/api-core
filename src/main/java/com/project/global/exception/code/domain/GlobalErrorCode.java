package com.project.global.exception.code.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
  EXAMPLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "EXAMPLE_001", "Example을 찾을 수 없습니다"),
  INTERNAL_SERVER_ERROR(HttpStatus.BAD_REQUEST, "EXAMPLE_001", "Example을 찾을 수 없습니다"),
  ;

  private final HttpStatus httpStatus;
  private final String customCode;
  private final String message;
}
