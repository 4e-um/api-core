package com.project.global.exception.core;

import com.project.global.exception.BaseException;
import com.project.global.exception.code.domain.BaseErrorCode;

public class OperationFailedException extends BaseException {
	public OperationFailedException(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
