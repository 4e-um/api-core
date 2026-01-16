package com.project.core.controller.dto.response;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import java.time.LocalDateTime;

public record SubscriptionResponse(
    Long subId,
    String maskedPhoneNumber,
    LocalDateTime startDate,
    LocalDateTime endDate,
    SubscriptionStatus status,
    Integer sendDay) {
  public static SubscriptionResponse from(Subscription s) {
    return new SubscriptionResponse(
        s.getSubId(),
        MaskingUtil.maskPhone(s.getPhoneNumber()),
        s.getStartDate(),
        s.getEndDate(),
        s.getStatus(),
        s.getSendDay());
  }
}
