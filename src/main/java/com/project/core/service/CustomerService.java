package com.project.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.controller.dto.response.MaskingUtil;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AesUtil aesUtil;

    @Transactional(readOnly = true)
    public Customer loadByContactEnc(String contactEnc) {
        return customerRepository
                .findByContactEnc(contactEnc)
                .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));
    }

    @Transactional
    public ChangeEmailResponse changeEmailEnc(
            Long customerId, ChangeEmailRequest request) { // 이메일 변경
        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.CUSTOMER_NOT_FOUND));

        String emailEnc = aesUtil.encrypt(request.email());
        customer.changeEmailEnc(emailEnc);

        String maskedEmail = MaskingUtil.maskEmail(request.email());
        return new ChangeEmailResponse(maskedEmail);
    }

    @Transactional
    public ChangeGradeResponse changeUserGrade(Long userId, ChangeGradeRequest request) { // 등급 변경
        Customer customer =
                customerRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.CUSTOMER_NOT_FOUND));

        customer.changeGrade(request.grade());
        return new ChangeGradeResponse(customer.getGrade());
    }
}
