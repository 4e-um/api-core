package com.project.core.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.response.SubscriptionDetailResponse;
import com.project.core.controller.dto.response.SubscriptionListResponse;
import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;
import com.project.global.util.ContactHashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final AesUtil aesUtil;
    private final ContactHashUtil contactHashUtil;

    /** 전체 회선 목록 조회 (페이징) */
    @Transactional(readOnly = true)
    public Page<SubscriptionListResponse> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository
                .findAll(pageable)
                .map(
                        sub -> {
                            String decryptedEmail = safeDecrypt(sub.getCustomer().getEmailEnc());
                            String decryptedPhone = safeDecrypt(sub.getPhoneNumber());
                            return SubscriptionListResponse.of(sub, decryptedEmail, decryptedPhone);
                        });
    }

    /** 전화번호 기반 회선 상세 조회 */
    @Transactional(readOnly = true)
    public SubscriptionDetailResponse getSubscriptionDetailByPhone(String phoneRaw) {
        String hash = contactHashUtil.hmacSha256Base64(phoneRaw);

        Subscription sub =
                subscriptionRepository
                        .findByPhoneHash(hash)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

        String decryptedEmail = safeDecrypt(sub.getCustomer().getEmailEnc());
        String decryptedPhone = safeDecrypt(sub.getPhoneNumber());

        // SubscriptionListResponse의 of 로직을 활용하여 기본 정보 추출
        SubscriptionListResponse baseInfo =
                SubscriptionListResponse.of(sub, decryptedEmail, decryptedPhone);

        return SubscriptionDetailResponse.from(baseInfo);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> findSubscriptionResponses(Long customerId) {
        List<Subscription> subscriptions =
                subscriptionRepository.findAllByCustomer_CustomerIdWithPlan(customerId);

        return subscriptions.stream().map(sub -> SubscriptionResponse.from(sub, aesUtil)).toList();
    }

    private String safeDecrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        try {
            return aesUtil.decrypt(encrypted);
        } catch (Exception e) {
            log.warn("Decryption failed: {}", encrypted);
            return encrypted;
        }
    }
}
