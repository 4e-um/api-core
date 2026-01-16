package com.project.global.exception.code.domain.core;

import com.project.global.exception.code.domain.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CoreErrorCode implements BaseErrorCode {
  // 고객
  CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOMER_001", "존재하지 않는 고객입니다."),

  // 할인정책
  DISCOUNT_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "DISCOUNT_POLICY_001", "존재하지 않는 할인 정책입니다."),

  // 할인
  DISCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "DISCOUNT_001", "존재하지 않는 할인입니다."),

  // 요금제
  PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN_001", "존재하지 않는 요금제입니다."),
  PLAN_ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "PLAN_002", "이미 가입된 요금제와 동일한 요금제로 변경할 수 없습니다."),

  // 부가서비스
  VAS_NOT_FOUND(HttpStatus.NOT_FOUND, "VAS_001", "존재하지 않는 부가서비스입니다."),
  VAS_ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "VAS_002", "이미 가입한 부가서비스입니다."),
  VAS_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "VAS_003", "이미 해지한 부가서비스입니다."),

  // 소액결제
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "MICRO_001", "유효하지 않은 금액입니다."),
  MICRO_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "MICRO_002", "존재하지 않는 결제 내역입니다."),
  MICRO_PAYMENT_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "MICRO_003", "이미 취소된 결제 내역입니다."),
  MICRO_PAYMENT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MICRO_004", "잘못된 접근입니다."),

  // 회선/가입
  SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_001", "존재하지 않는 회선입니다."),
  SUBSCRIPTION_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "SUB_002", "이미 해지된 회선입니다."),
  PHONE_NUMBER_GENERATION_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR, "SUB_003", "사용 가능한 전화번호 생성에 실패했습니다. (재시도 횟수 초과)"),

  // 보안
  ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SEC_001", "암호화/복호화 처리 중 오류가 발생했습니다."),
  AES_KEY_LENGTH_INCORRECT(
      HttpStatus.INTERNAL_SERVER_ERROR, "SEC_002", "AES Secret Key는 32바이트여야 합니다."),
  ;

  private final HttpStatus httpStatus;
  private final String customCode;
  private final String message;
}
