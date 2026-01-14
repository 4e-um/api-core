package com.project.core.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CustomerController {
	private final CustomerService customerService;

    /**
     * 고객 조회 (전화번호 기준)
     */
    @GetMapping
    public ResponseEntity<List<Customer>> loadByContactEnc(
            @RequestParam String contactEnc
    ) {
        List<Customer> customers = customerService.loadByContactEnc(contactEnc);
        return ResponseEntity.ok(customers);
    }

    /**
     * 이메일 변경
     * @throws Exception 
     */
    @PostMapping("/{userId}/email")
    public ResponseEntity<ChangeEmailResponse> changeEmail(
            @PathVariable Long userId,
            @RequestBody ChangeEmailRequest request
    ) throws Exception {
        ChangeEmailResponse response =
                customerService.changeEmailEnc(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 고객 등급 변경
     */
    @PostMapping("/{userId}/grade")
    public ResponseEntity<ChangeGradeResponse> changeGrade(
            @PathVariable Long userId,
            @RequestBody ChangeGradeRequest request
    ) {
        ChangeGradeResponse response =
                customerService.changeUserGrade(userId, request);
        return ResponseEntity.ok(response);
    }

}
