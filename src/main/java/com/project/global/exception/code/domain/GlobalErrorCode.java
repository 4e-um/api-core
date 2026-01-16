package com.project.global.exception.code.domain;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "COMMON_002", "올바르지 않은 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_003", "지원하지 않은 Http Method 입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_004", "서버 에러가 발생했습니다."),
    BLOCKED_API(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "운영 환경에서 사용할 수 없는 API 입니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
