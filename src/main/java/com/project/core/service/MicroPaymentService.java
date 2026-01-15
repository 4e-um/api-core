package com.project.core.service;

import com.project.core.controller.dto.response.MicroPaymentResponse;
import com.project.core.infra.entity.micro.MicroPayment;
import com.project.core.infra.entity.micro.enums.MicroPaymentStatus;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.repository.micro.MicroPaymentRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MicroPaymentService {

  private final MicroPaymentRepository microPaymentRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final Clock clock;

  // 소액결제 승인
  public MicroPaymentResponse pay(Long subId, String name, Integer amount) {
    // 회선 상태 확인
    Subscription subscription =
        subscriptionRepository
            .findById(subId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

    if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    // 금액 유효성 검사
    if (amount <= 0) {
      throw new InvalidStateException(CoreErrorCode.INVALID_INPUT_VALUE);
    }

    // 결제 내역 생성
    MicroPayment microPayment =
        MicroPayment.builder()
            .subscription(subscription)
            .name(name)
            .amount(amount)
            .clock(clock)
            .build();
    MicroPayment savedMicroPayment = microPaymentRepository.save(microPayment);

    return toResponse(savedMicroPayment);
  }

  // 소액결제 취소
  public MicroPaymentResponse cancel(Long microId, Long subId) {
    // 결제 내역 조회
    MicroPayment microPayment =
        microPaymentRepository
            .findById(microId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND));

    // 결제 내역의 주인과 요청자가 다르면 예외 발생
    if (!microPayment.getSubscription().getSubId().equals(subId)) {
      throw new InvalidStateException(CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST);
    }

    // 이미 취소된 건인지 확인
    if (microPayment.getStatus() == MicroPaymentStatus.CANCELED) {
      throw new InvalidStateException(CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED);
    }

    // 취소 처리
    microPayment.cancel();

    return toResponse(microPayment);
  }

  private MicroPaymentResponse toResponse(MicroPayment entity) {
    return new MicroPaymentResponse(
        entity.getMicroId(),
        entity.getSubscription().getSubId(),
        entity.getName(),
        entity.getAmount(),
        entity.getPayDate(),
        entity.getStatus().name());
  }
}
