package com.project.core.controller;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.controller.dto.response.CustomerResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.service.CustomerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;

  /** 고객 조회 (전화번호 기준) */
  @GetMapping
  public ResponseEntity<List<CustomerResponse>> loadByContactEnc(@RequestParam String contactEnc) {
    List<Customer> customers = customerService.loadByContactEnc(contactEnc);

    List<CustomerResponse> response = customers.stream().map(CustomerResponse::from).toList();

    return ResponseEntity.ok(response);
  }

  /** 이메일 변경 */
  @PostMapping("/{userId}/email")
  public ResponseEntity<ChangeEmailResponse> changeEmail(
      @PathVariable Long userId, @RequestBody ChangeEmailRequest request) {
    ChangeEmailResponse response = customerService.changeEmailEnc(userId, request);
    return ResponseEntity.ok(response);
  }

  /** 고객 등급 변경 */
  @PostMapping("/{userId}/grade")
  public ResponseEntity<ChangeGradeResponse> changeGrade(
      @PathVariable Long userId, @RequestBody ChangeGradeRequest request) {
    ChangeGradeResponse response = customerService.changeUserGrade(userId, request);
    return ResponseEntity.ok(response);
  }
}
