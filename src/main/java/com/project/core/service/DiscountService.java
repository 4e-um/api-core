package com.project.core.service;

import com.project.core.infra.entity.discount.DiscountPolicy;
import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.discount.DiscountPolicyRepository;
import com.project.core.infra.repository.discount.SubscriptionDiscountRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscountService {

  private final SubscriptionDiscountRepository subscriptionDiscountRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final DiscountPolicyRepository discountPolicyRepository;

  @Transactional(readOnly = true)
  public List<SubscriptionDiscount> loadRequiredBySubId(Long subId) {
    List<SubscriptionDiscount> discounts =
        subscriptionDiscountRepository.findBySubscription_SubId(subId);

    if (discounts.isEmpty()) {
      throw new EntityNotFoundException(CoreErrorCode.DISCOUNT_NOT_FOUND);
    }
    return discounts;
  }

  // 트랜잭션 애노테이션 제거 (또는 유지해도 되지만, 내부호출 문제를 없애려면 경계 통일이 깔끔)
  public Long addDiscount(Long subId, Long discountId) {
    SubscriptionDiscount discount = createSubscriptionDiscount(subId, discountId);
    subscriptionDiscountRepository.save(discount);
    return discount.getSdId();
  }

  @Transactional
  public Long changeDiscount(Long discountId, Long sdId) {
    SubscriptionDiscount old =
        subscriptionDiscountRepository
            .findBySdId(sdId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.DISCOUNT_NOT_FOUND));

    old.setEndDate(LocalDateTime.now());
    old.setStatusTerminated();

    SubscriptionDiscount created =
        createSubscriptionDiscount(old.getSubscription().getSubId(), discountId);

    subscriptionDiscountRepository.save(created);
    return created.getSdId();
  }

  private SubscriptionDiscount createSubscriptionDiscount(Long subId, Long discountId) {
    Subscription subscription =
        subscriptionRepository
            .findById(subId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

    DiscountPolicy policy =
        discountPolicyRepository
            .findById(discountId)
            .orElseThrow(
                () -> new EntityNotFoundException(CoreErrorCode.DISCOUNT_POLICY_NOT_FOUND));

    return SubscriptionDiscount.builder()
        .discountPolicy(policy)
        .subscription(subscription)
        .discountType(policy.getDiscountType())
        .value(policy.getValue())
        .targetScope(policy.getTargetScope())
        .startDate(LocalDateTime.now())
        .build();
  }
}
