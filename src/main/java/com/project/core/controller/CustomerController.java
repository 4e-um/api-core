package com.project.core.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.request.PhoneSearchRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.controller.dto.response.CustomerListResponse;
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

    /** 고객 전체 목록 조회 (페이징) */
    @GetMapping
    public ResponseEntity<Page<CustomerListResponse>> getAllCustomers(
            @RequestParam(name = "search", required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Page<CustomerListResponse> response = customerService.getAllCustomers(search, pageable);
        return ResponseEntity.ok(response);
    }

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

    /** 고객 상세 조회 (ID 기준) */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerListResponse> getCustomerDetail(
            @PathVariable("customerId") Long customerId) {
        CustomerListResponse response = customerService.getCustomerDetail(customerId);
        return ResponseEntity.ok(response);
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
