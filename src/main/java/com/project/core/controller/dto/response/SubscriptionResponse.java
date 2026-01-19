package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.global.util.AesUtil;

public record SubscriptionResponse(
    Long subId,
    String maskedPhoneNumber,
    LocalDateTime startDate,
    LocalDateTime endDate,
    SubscriptionStatus status,
    Integer sendDay
) {
  public static SubscriptionResponse from(Subscription subscription, AesUtil aesUtil) {
    String decryptedPhone = aesUtil.decrypt(subscription.getPhoneNumber()); // ✅ 복호화
    String maskedPhone = MaskingUtil.maskPhone(decryptedPhone);            // ✅ 마스킹

    return new SubscriptionResponse(
        subscription.getSubId(),
        maskedPhone,
        subscription.getStartDate(),
        subscription.getEndDate(),
        subscription.getStatus(),
        subscription.getSendDay()
    );
  }
}
