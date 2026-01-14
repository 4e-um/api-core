package com.project.core.controller.dto.response;

import com.project.core.infra.entity.customer.Customer;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerResponse {

    private Long customerId;
    private String name;
    private String grade;

    // 필요하면 마스킹된 연락처만 제공(원본/암호화값은 노출 금지)
    private String maskedContact;

    public static CustomerResponse from(Customer c) {
        return CustomerResponse.builder()
                .customerId(c.getCustomerId())   // ✅ 실제 PK getter명으로 맞추기
                .name(c.getName())               // ✅ 실제 필드명으로 맞추기
                .grade(String.valueOf(c.getGrade())) // grade가 enum이면 적절히 변환
                // maskedContact는 "복호화 가능한 원본"이 있을 때만 넣는 걸 권장
                .maskedContact(null)
                .build();
    }
}
