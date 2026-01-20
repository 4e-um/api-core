package com.project.core.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.controller.dto.response.CustomerListResponse;
import com.project.core.controller.dto.response.MaskingUtil;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;
import com.project.global.util.ContactHashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ... (기존 import 유지)

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AesUtil aesUtil;
    private final ContactHashUtil contactHashUtil;

    @Transactional(readOnly = true)
    public Page<CustomerListResponse> getAllCustomers(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            // 검색어가 있을 경우: 전화번호 해싱 후 검색
            try {
                String searchHash = contactHashUtil.hmacSha256Base64(search);
                Optional<Customer> customerOpt = customerRepository.findByContactHash(searchHash);

                if (customerOpt.isPresent()) {
                    Customer customer = customerOpt.get();
                    // 단건 결과를 Page로 변환 (DTO 변환 로직 재사용 필요하므로 아래 로직 태움)
                    return new PageImpl<>(List.of(customer), pageable, 1).map(c -> convertToDto(c));
                } else {
                    return Page.empty(pageable);
                }
            } catch (Exception e) {
                // 해싱 실패 시 빈 결과
                return Page.empty(pageable);
            }
        }

        // 전체 조회
        return customerRepository.findAll(pageable).map(this::convertToDto);
    }

    private CustomerListResponse convertToDto(Customer customer) {
        // 1. 고객 연락처 및 이메일 복호화
        String decryptedContact = safeDecrypt(customer.getContactEnc());
        String decryptedEmail = safeDecrypt(customer.getEmailEnc());

        // 2. 대표 회선 찾기
        Subscription sub =
                customer.getSubscriptionHistory().stream()
                        .filter(s -> "ACTIVE".equals(s.getStatus().name()))
                        .reduce((first, second) -> second)
                        .orElse(
                                customer.getSubscriptionHistory().isEmpty()
                                        ? null
                                        : customer.getSubscriptionHistory()
                                                .get(customer.getSubscriptionHistory().size() - 1));

        // 3. 회선 전화번호 복호화
        String decryptedSubPhone = null;
        if (sub != null) {
            decryptedSubPhone = safeDecrypt(sub.getPhoneNumber());
        }

        // 4. DTO 생성
        return CustomerListResponse.of(
                customer, sub, decryptedContact, decryptedEmail, decryptedSubPhone);
    }

    private String safeDecrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        try {
            return aesUtil.decrypt(encrypted);
        } catch (Exception e) {
            // 복호화 실패 시 (평문이거나 형식이 안 맞음) 원본 반환 혹은 로깅
            // log.warn("Decryption failed for value: {}", encrypted, e);
            return encrypted;
        }
    }

    @Transactional(readOnly = true)
    public Customer loadByPhone(String phoneRaw) {
        String hash = contactHashUtil.hmacSha256Base64(phoneRaw);

        return customerRepository
                .findByContactHash(hash)
                .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public CustomerListResponse getCustomerDetail(Long customerId) {
        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.CUSTOMER_NOT_FOUND));
        return convertToDto(customer);
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
