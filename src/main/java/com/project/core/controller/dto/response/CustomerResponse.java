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

    public static CustomerResponse from(Customer customer) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .grade(customer.getGrade().name())
                .build();
    }
}
