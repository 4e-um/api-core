package com.project.global.exception.core;

import com.project.global.exception.BaseException;
import com.project.global.exception.code.domain.BaseErrorCode;

public class EntityNotFoundException extends BaseException {
  public EntityNotFoundException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
