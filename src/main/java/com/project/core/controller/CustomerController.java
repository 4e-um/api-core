package com.project.core.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.request.PhoneSearchRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.controller.dto.response.CustomerSearchResponse;
import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.service.CustomerService;
import com.project.core.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final SubscriptionService subscriptionService;

    /** 고객 조회 (전화번호 기준) */
    @PostMapping("/search")
    public ResponseEntity<CustomerSearchResponse> searchByPhone(
            @RequestBody PhoneSearchRequest request) {
        Customer customer = customerService.loadByPhone(request.phoneRaw());
        Long customerId = customer.getCustomerId();

        List<SubscriptionResponse> subscriptions =
                subscriptionService.findSubscriptionResponses(customerId);

        return ResponseEntity.ok(
                new CustomerSearchResponse(customerId, customer.getName(), subscriptions));
    }

    /** 이메일 변경 */
    @PostMapping("/{customerId}/email")
    public ResponseEntity<ChangeEmailResponse> changeEmail(
            @PathVariable("customerId") Long customerId, @RequestBody ChangeEmailRequest request) {
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);
        return ResponseEntity.ok(response);
    }

    /** 고객 등급 변경 */
    @PostMapping("/{customerId}/grade")
    public ResponseEntity<ChangeGradeResponse> changeGrade(
            @PathVariable(name = "customerId") Long customerId,
            @RequestBody ChangeGradeRequest request) {
        ChangeGradeResponse response = customerService.changeUserGrade(customerId, request);
        return ResponseEntity.ok(response);
    }
}
