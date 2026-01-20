package com.project.core.controller.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import com.project.core.infra.entity.plan.Plan;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.global.util.AesUtil;

public record SubscriptionResponse(
        Long subId,
        String maskedPhoneNumber,
        String startDate, // yyyy-MM-dd
        String endDate,   // yyyy-MM-dd
        SubscriptionStatus status,
        Integer sendDay,
        String planName,
        long dataUsed, // Byte
        long dataLimit // Byte
        ) {
    public static SubscriptionResponse from(Subscription subscription) {
        String currentPlan = "N/A";
        long dataLimit = 0;
        long dataUsed = 0;

        // 시드 기반 랜덤 (회선 ID)
        Random random = new Random(subscription.getSubId());

        if (subscription.getPlanHistory() != null && !subscription.getPlanHistory().isEmpty()) {
            Plan plan = subscription.getPlanHistory()
                    .get(subscription.getPlanHistory().size() - 1)
                    .getPlan();
            currentPlan = plan.getPlanName();

            long allotmentMB = plan.getAllotmentAmount();
            if (allotmentMB == -1) {
                dataLimit = -1;
                dataUsed = (long) (random.nextDouble() * 50L * 1024 * 1024 * 1024);
            } else {
                dataLimit = allotmentMB * 1024 * 1024;
                dataUsed = (long) (random.nextDouble() * dataLimit);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStart = subscription.getStartDate().format(formatter);
        String formattedEnd = subscription.getEndDate() != null ? subscription.getEndDate().format(formatter) : null;

        return new SubscriptionResponse(
                subscription.getSubId(),
                MaskingUtil.maskPhone(subscription.getPhoneNumber()),
                formattedStart,
                formattedEnd,
                subscription.getStatus(),
                subscription.getSendDay(),
                currentPlan,
                dataUsed,
                dataLimit);
    }
}
