package com.project.core.controller.dto.response;

import lombok.Builder;

@Builder
public record SubscriptionDetailResponse(
        Long subId,
        String customerName,
        String email, // 마스킹됨
        String phoneNumber, // 마스킹됨
        String planName,
        long dataUsed, // Byte
        long dataLimit, // Byte (-1 if unlimited)
        String status,
        String startDate, // yyyy-MM-dd
        String lastActivity) {

    public static SubscriptionDetailResponse from(SubscriptionListResponse baseInfo) {
        return new SubscriptionDetailResponse(
                baseInfo.subId(),
                baseInfo.customerName(),
                baseInfo.email(),
                baseInfo.phoneNumber(),
                baseInfo.planName(),
                baseInfo.dataUsed(),
                baseInfo.dataLimit(),
                baseInfo.status(),
                baseInfo.startDate(),
                baseInfo.lastActivity());
    }
}
