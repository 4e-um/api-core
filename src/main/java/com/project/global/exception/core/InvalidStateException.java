package com.project.global.exception.core;

import com.project.global.exception.BaseException;
import com.project.global.exception.code.domain.BaseErrorCode;

public class InvalidStateException extends BaseException {
    public InvalidStateException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidStateException(BaseErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
