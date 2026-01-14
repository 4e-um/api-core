package com.project.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.request.FindCustomerRequest;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerService customerService;

  @GetMapping
  public Long save(@RequestBody FindCustomerRequest request) {
      Customer customer = customerService.loadByContactEnc(request.contactEnc());
      return customer.getCustomerId();
  }
  
}
