package com.project.global.exception.code.domain.core;

import org.springframework.http.HttpStatus;

import com.project.global.exception.code.domain.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoreErrorCode implements BaseErrorCode {
    // Customer
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOMER_001", "존재하지 않는 고객입니다."),

    // Discount policy
    DISCOUNT_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "DISCOUNT_POLICY_001", "존재하지 않는 할인 정책입니다."),

    // Discount
    DISCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "DISCOUNT_001", "존재하지 않는 할인입니다."),

    // Plan
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN_001", "존재하지 않는 요금제입니다."),
    PLAN_ALREADY_SUBSCRIBED(
            HttpStatus.BAD_REQUEST, "PLAN_002", "이미 가입된 요금제는 동일 요금제로 변경할 수 없습니다."),

    // VAS
    VAS_NOT_FOUND(HttpStatus.NOT_FOUND, "VAS_001", "존재하지 않는 부가서비스입니다."),
    VAS_ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "VAS_002", "이미 가입된 부가서비스입니다."),
    VAS_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "VAS_003", "이미 해지된 부가서비스입니다."),

    // Micro payment
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "MICRO_001", "유효하지 않은 금액입니다."),
    MICRO_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "MICRO_002", "존재하지 않는 결제 내역입니다."),
    MICRO_PAYMENT_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "MICRO_003", "이미 취소된 결제 내역입니다."),
    MICRO_PAYMENT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MICRO_004", "잘못된 요청입니다."),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_001", "존재하지 않는 회선입니다."),
    SUBSCRIPTION_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "SUB_002", "이미 해지된 회선입니다."),
    PHONE_NUMBER_GENERATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR, "SUB_003", "사용 가능한 전화번호 생성에 실패했습니다. (재시도 횟수 초과)"),
    SUBSCRIPTION_SUSPENDED(HttpStatus.BAD_REQUEST, "SUB_004", "정지된 회선입니다."),

    // Security
    ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SEC_001", "암호화/복호화 처리 중 오류가 발생했습니다."),
    AES_KEY_LENGTH_INCORRECT(
            HttpStatus.INTERNAL_SERVER_ERROR, "SEC_002", "AES Secret Key는 32바이트여야 합니다."),
    ENCRYPTION_MUST_BE_BASE64(
            HttpStatus.INTERNAL_SERVER_ERROR, "SEC_003", "암호화 값은 base64로 인코딩되어야 합니다."),
    HASHING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SEC_004", "해싱에 실패했습니다."),

    // Batch
    DASHBOARD_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "BATCH_001", "대시보드 로딩에 실패했습니다."),

    // Template
    TEMPLATE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_001", "템플릿 그룹을 찾을 수 없습니다."),
    TEMPLATE_GROUP_CODE_DUPLICATED(HttpStatus.CONFLICT, "TEMPLATE_002", "이미 사용 중인 템플릿 그룹 코드입니다."),
    TEMPLATE_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_003", "템플릿 버전을 찾을 수 없습니다."),
    TEMPLATE_VERSION_ACTIVE_CANNOT_UPDATE(
            HttpStatus.CONFLICT, "TEMPLATE_004", "ACTIVE인 템플릿 버전은 수정할 수 없습니다."),
    TEMPLATE_VERSION_ACTIVE_CANNOT_DELETE(
            HttpStatus.CONFLICT, "TEMPLATE_005", "ACTIVE인 템플릿 버전은 삭제할 수 없습니다."),
    TEMPLATE_VARIABLE_MISSING(
            HttpStatus.BAD_REQUEST, "TEMPLATE_006", "필수 템플릿 변수가 누락되었습니다."),
    TEMPLATE_SUBJECT_REQUIRED(
            HttpStatus.BAD_REQUEST, "TEMPLATE_007", "EMAIL 템플릿은 subject가 필요합니다."),
    TEMPLATE_BODY_REQUIRED(
            HttpStatus.BAD_REQUEST, "TEMPLATE_008", "템플릿 본문(body)은 필수입니다."),
    TEMPLATE_CHANNEL_REQUIRED(
            HttpStatus.BAD_REQUEST, "TEMPLATE_009", "템플릿 채널(channel)은 필수입니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "TEMPLATE_999", "템플릿을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

