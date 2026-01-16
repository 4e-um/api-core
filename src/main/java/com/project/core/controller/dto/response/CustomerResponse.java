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

  public static CustomerResponse from(Customer c) {
    return CustomerResponse.builder()
        .customerId(c.getCustomerId())
        .name(c.getName())
        .grade(c.getGrade().name())
        .build();
  }
}
