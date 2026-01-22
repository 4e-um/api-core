package com.project.global.exception.code.domain.notification;

import org.springframework.http.HttpStatus;

import com.project.global.exception.code.domain.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {
    LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "존재하지 않는 로그입니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
