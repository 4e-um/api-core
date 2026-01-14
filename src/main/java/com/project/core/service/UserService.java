package com.project.core.service;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  @Transactional
  public Customer loadByContactEnc(String contactEnc) {
    return userRepository
        .findByContactEnc(contactEnc)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
  }

  @Transactional
  public ChangeEmailResponse changeEmailEnc(Long userId, ChangeEmailRequest request) {
    Customer customer =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

    customer.changeEmailEnc(request.emailEnc());
    return new ChangeEmailResponse(customer.getEmailEnc());
  }

  @Transactional
  public ChangeGradeResponse changeUserGrade(Long userId, ChangeGradeRequest request) {
    Customer customer =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

    customer.changeGrade(request.grade());
    return new ChangeGradeResponse(customer.getGrade());
  }
}
