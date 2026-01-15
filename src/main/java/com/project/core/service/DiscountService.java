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

  @Transactional(readOnly = true) // 회선에 적용 된 할인 정책 확인
  public List<SubscriptionDiscount> loadRequiredBySubId(Long subId) {

    List<SubscriptionDiscount> discounts =
        subscriptionDiscountRepository.findBySubscription_SubId(subId);

    if (discounts.isEmpty()) {
      throw new EntityNotFoundException(CoreErrorCode.DISCOUNT_NOT_FOUND);
    }

    return discounts;
  }

  @Transactional
  public Long addDiscount(Long subId, Long discountId) {

    Subscription subscription =
        subscriptionRepository
            .findById(subId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

    DiscountPolicy policy =
        discountPolicyRepository
            .findById(discountId)
            .orElseThrow(
                () -> new EntityNotFoundException(CoreErrorCode.DISCOUNT_POLICY_NOT_FOUND));

    SubscriptionDiscount discount =
        SubscriptionDiscount.builder()
            .discountPolicy(policy)
            .subscription(subscription)
            .discountType(policy.getDiscountType())
            .value(policy.getValue())
            .targetScope(policy.getTargetScope())
            .startDate(LocalDateTime.now())
            .build();

    subscriptionDiscountRepository.save(discount);

    return discount.getSdId();
  }

  @Transactional
  public Long changeDiscount(Long discountId, Long sdId) {

    SubscriptionDiscount discount =
        subscriptionDiscountRepository
            .findBySdId(sdId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.DISCOUNT_NOT_FOUND));
    // 내부 정보 수정 - 기존 할인 종료
    discount.setEndDate(LocalDateTime.now());
    discount.setStatusTerminated();
    // 새로운 할인 적용
    return addDiscount(discount.getSubscription().getSubId(), discountId); // 생성된 할인 테이블 아이디 반환
  }
}
