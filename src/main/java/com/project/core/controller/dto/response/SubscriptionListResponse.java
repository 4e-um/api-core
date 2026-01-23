package com.project.core.controller.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import com.project.core.infra.entity.plan.Plan;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.subscription.Subscription;

import lombok.Builder;

@Builder
public record SubscriptionListResponse(
        Long subId,
        String representativeSubId, // 포맷팅됨: SUB-0000001
        String customerName,
        String email, // 마스킹됨
        String phoneNumber, // 마스킹됨
        String planName,
        long dataUsed, // Byte
        long dataLimit, // Byte (-1 if unlimited)
        String status,
        String startDate, // yyyy-MM-dd
        String lastActivity) { // yyyy-MM-dd HH:mm

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static SubscriptionListResponse of(
            Subscription sub, long totalUsedAmount, long allotmentAmount,
            String decryptedEmail, String decryptedPhone) {
        String planName = "N/A";

        // 회선 ID 포맷팅 (SUB-0000001)
        String formattedSubId = null;
        if (sub != null) {
            formattedSubId = String.format("SUB-%07d", sub.getSubId());
        }

        // 날짜 포맷팅
        String formattedStartDate = sub.getStartDate().format(DATE_FORMATTER);

        // 최근 활동 계산 (회선 ID 기준 시드)
        Random subRandom = new Random(sub.getSubId());
        long randomDays = (long) (subRandom.nextDouble() * 30);
        long randomHours = (long) (subRandom.nextDouble() * 24);

        LocalDateTime randomActivity =
                LocalDateTime.now().minusDays(randomDays).minusHours(randomHours);
        String formattedLastActivity = randomActivity.format(DATE_TIME_FORMATTER);

        return SubscriptionListResponse.builder()
                .subId(sub.getSubId())
                .representativeSubId(formattedSubId)
                .customerName(sub.getCustomer().getName())
                .email(MaskingUtil.maskEmail(decryptedEmail))
                .phoneNumber(MaskingUtil.maskPhone(decryptedPhone))
                .planName(planName)
                .dataUsed(totalUsedAmount)
                .dataLimit(allotmentAmount)
                .status(sub.getStatus().name())
                .startDate(formattedStartDate)
                .lastActivity(formattedLastActivity)
                .build();
    }
}
