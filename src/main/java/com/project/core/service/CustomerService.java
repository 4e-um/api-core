package com.project.core.service;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final AesUtil aesUtil;

  @Transactional(readOnly = true)
  public Customer loadByContactEnc(String contactEnc) {
      return customerRepository.findByContactEnc(contactEnc)
          .orElseThrow(() ->
              new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND)
          );
  }


  @Transactional
  public ChangeEmailResponse changeEmailEnc(Long customerId, ChangeEmailRequest request) { // 이메일 변경
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

    String emailEnc = aesUtil.encrypt(request.email());
    customer.changeEmailEnc(emailEnc);
    
    String maskedEmail = maskEmail(request.email());
    return new ChangeEmailResponse(maskedEmail);
  }

  @Transactional
  public ChangeGradeResponse changeUserGrade(Long userId, ChangeGradeRequest request) { // 등급 변경
    Customer customer =
        customerRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

    customer.changeGrade(request.grade());
    return new ChangeGradeResponse(customer.getGrade());
  }
  
  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return null;
    }
    String[] parts = email.split("@", 2);
    String local = parts[0];
    String domain = parts[1];

    if (local.isEmpty()) {
      return "***@" + domain;
    }
    if (local.length() == 1) {
      return local + "***@" + domain;
    }
    // 첫 글자만 남기고 나머지 마스킹
    return local.charAt(0) + "***@" + domain;
  }
}
