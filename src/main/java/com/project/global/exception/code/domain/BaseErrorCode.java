package com.project.global.exception.code.domain;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  String name(); // ⭐ 핵심

  HttpStatus getHttpStatus();

  String getMessage();

  String getCustomCode();
}
