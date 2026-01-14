package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.Status;
import com.project.core.infra.entity.discount.enums.TargetScope;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionDiscountResponse {

    private Long sdId;
    private Long subId;
    private String maskedPhoneNumber;

    private Long discountId;
    private DiscountType discountType;
    private Double value;
    private TargetScope targetScope;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Status status;

    public static SubscriptionDiscountResponse from(SubscriptionDiscount sd) {

        // ⚠️ Subscription 엔티티 실제 getter명으로 수정
        String phone = sd.getSubscription().getPhoneNumber();

        return SubscriptionDiscountResponse.builder()
                .sdId(sd.getSdId())
                .subId(sd.getSubscription().getSubId())
                .maskedPhoneNumber(maskPhone(phone))

                .discountId(sd.getDiscountPolicy().getDiscountId())
                .discountType(sd.getDiscountType())
                .value(sd.getValue())
                .targetScope(sd.getTargetScope())

                .startDate(sd.getStartDate())
                .endDate(sd.getEndDate())
                .status(sd.getStatus())
                .build();
    }

    /**
     * 010-1234-1212 -> 010-**12-**12
     * 01012341212   -> 010-**12-**12
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return null;

        // 숫자만 추출
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 11) {
            return "****"; // 예상 못 한 형식은 전체 마스킹
        }

        String p1 = digits.substring(0, 3);   // 010
        String p2 = digits.substring(3, 7);   // 1234
        String p3 = digits.substring(7, 11);  // 1212

        // **12 / **12
        String masked2 = "**" + p2.substring(2);
        String masked3 = "**" + p3.substring(2);

        return String.format("%s-%s-%s", p1, masked2, masked3);
    }
}
