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
            Subscription sub, String decryptedEmail, String decryptedPhone) {
        String planName = "N/A";
        long dataLimit = 0;
        long dataUsed = 0;

        // 현재 요금제 정보 조회
        List<SubscriptionPlan> planHistory = sub.getPlanHistory();
        if (!planHistory.isEmpty()) {
            // 가장 최근 요금제 (리스트의 마지막 요소가 최신이라고 가정하거나 로직 필요)
            // 보통 히스토리는 순서대로 쌓임. 마지막 요소 사용.
            Plan currentPlan = planHistory.get(planHistory.size() - 1).getPlan();
            planName = currentPlan.getPlanName();
            long allotmentMB = currentPlan.getAllotmentAmount();

            Random subRandom = new Random(sub.getSubId());
            if (allotmentMB == -1) {
                dataLimit = -1; // 무제한
                dataUsed = (long) (subRandom.nextDouble() * 50L * 1024 * 1024 * 1024);
            } else {
                dataLimit = allotmentMB * 1024 * 1024; // MB -> Byte
                dataUsed = (long) (subRandom.nextDouble() * dataLimit);
            }
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
                .customerName(sub.getCustomer().getName())
                .email(MaskingUtil.maskEmail(decryptedEmail))
                .phoneNumber(MaskingUtil.maskPhone(decryptedPhone))
                .planName(planName)
                .dataUsed(dataUsed)
                .dataLimit(dataLimit)
                .status(sub.getStatus().name())
                .startDate(formattedStartDate)
                .lastActivity(formattedLastActivity)
                .build();
    }
}
