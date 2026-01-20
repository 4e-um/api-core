package com.project.core.controller.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.plan.Plan;
import com.project.core.infra.entity.subscription.Subscription;

import lombok.Builder;

@Builder
public record CustomerListResponse(
        Long customerId,
        String name,
        String contact, // 마스킹됨: 010-**34-**78
        String email, // 마스킹됨: te***@example.com
        String grade,
        String representativeSubId, // 포맷팅됨: SUB-0000001
        String representativePhone, // 마스킹됨
        String subStatus,
        String planName,
        long dataUsed, // Byte
        long dataLimit, // Byte (-1 if unlimited)
        String createdAt, // yyyy-MM-dd
        String lastActivity) { // yyyy-MM-dd HH:mm

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static CustomerListResponse of(
            Customer customer,
            Subscription sub,
            String decryptedContact,
            String decryptedEmail,
            String decryptedSubPhone) {

        String planName = "N/A";
        long dataLimit = 0;
        long dataUsed = 0;

        // 1. 데이터 사용량 계산 (회선 ID 기준 시드)
        if (sub != null && !sub.getPlanHistory().isEmpty()) {
            Plan currentPlan = sub.getPlanHistory().get(sub.getPlanHistory().size() - 1).getPlan();
            planName = currentPlan.getPlanName();
            long allotmentMB = currentPlan.getAllotmentAmount();

            // 상세 보기와 동일한 값을 위해 subId만을 시드로 사용
            Random subRandom = new Random(sub.getSubId());

            if (allotmentMB == -1) {
                dataLimit = -1; // 무제한
                dataUsed = (long) (subRandom.nextDouble() * 50L * 1024 * 1024 * 1024);
            } else {
                dataLimit = allotmentMB * 1024 * 1024; // MB -> Byte
                dataUsed = (long) (subRandom.nextDouble() * dataLimit);
            }
        }

        // 회선 ID 포맷팅 (SUB-0000001)
        String formattedSubId = null;
        if (sub != null) {
            formattedSubId = String.format("SUB-%07d", sub.getSubId());
        }

        // 날짜 포맷팅
        String formattedCreatedAt = customer.getCreatedAt().format(DATE_FORMATTER);

        // 2. 최근 활동 계산 (고객 ID 기준 시드) - 목록/상세 어디서든 고객 기준이면 동일하게 나오도록
        Random customerRandom = new Random(customer.getCustomerId());
        long randomDays = (long) (customerRandom.nextDouble() * 60);
        long randomHours = (long) (customerRandom.nextDouble() * 24);
        long randomMinutes = (long) (customerRandom.nextDouble() * 60);

        LocalDateTime randomActivity =
                LocalDateTime.now()
                        .minusDays(randomDays)
                        .minusHours(randomHours)
                        .minusMinutes(randomMinutes);
        String formattedLastActivity = randomActivity.format(DATE_TIME_FORMATTER);

        return CustomerListResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .contact(MaskingUtil.maskPhone(decryptedContact))
                .email(MaskingUtil.maskEmail(decryptedEmail)) // 커스텀 마스킹 사용
                .grade(customer.getGrade().name())
                .representativeSubId(formattedSubId)
                .representativePhone(sub != null ? MaskingUtil.maskPhone(decryptedSubPhone) : "N/A")
                .subStatus(sub != null ? sub.getStatus().name() : "NONE")
                .planName(planName)
                .dataUsed(dataUsed)
                .dataLimit(dataLimit)
                .createdAt(formattedCreatedAt)
                .lastActivity(formattedLastActivity)
                .build();
    }
}
