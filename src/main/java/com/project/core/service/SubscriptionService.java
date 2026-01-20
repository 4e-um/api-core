package com.project.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.util.AesUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final AesUtil aesUtil;

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> findSubscriptionResponses(Long customerId) {
        List<Subscription> subscriptions =
                subscriptionRepository.findAllByCustomer_CustomerIdWithPlan(customerId);

        // ✅ 구독이 없으면 빈 리스트 반환 (예외 없음)
        return subscriptions.stream().map(sub -> SubscriptionResponse.from(sub, aesUtil)).toList();
    }
}
