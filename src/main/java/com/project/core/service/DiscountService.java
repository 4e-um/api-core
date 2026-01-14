package com.project.core.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.infra.entity.discount.DiscountPolicy;
import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.Status;
import com.project.core.infra.entity.discount.enums.TargetScope;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.discount.DiscountPolicyRepository;
import com.project.core.infra.repository.discount.SubscriptionDiscountRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class DiscountService {
	
	private final SubscriptionDiscountRepository subscriptionDiscountRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final DiscountPolicyRepository discountPolicyRepository;
	
	@Transactional(readOnly = true)		//회선에 적용 된 할인 정책 확인
	public List<SubscriptionDiscount> loadRequiredBySubId(Long subId) {

	    List<SubscriptionDiscount> discounts =
	            subscriptionDiscountRepository.findBySubId(subId);

	    if (discounts.isEmpty()) {
	        throw new IllegalStateException("해당 회선에 적용된 할인이 없습니다");
	    }

	    return discounts;
	}
	
	@Transactional
	public Long addDiscount(Long subId, Long discountId) {

	    Subscription subscription =
	        subscriptionRepository.findById(subId)
	            .orElseThrow(() -> new IllegalArgumentException("회선이 존재하지 않습니다"));

	    DiscountPolicy policy =
	        discountPolicyRepository.findById(discountId)
	            .orElseThrow(() -> new IllegalArgumentException("할인 정책이 존재하지 않습니다"));

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
	public Long changeDiscount(Long discountId,Long sdId) {	//할인 변경
		SubscriptionDiscount discount = 
				subscriptionDiscountRepository.findBySdId(sdId)
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 할인입니다."));
		//내부 정보 수정 - 기존 할인 종료
		discount.setEndDate(LocalDateTime.now());
		discount.setStatusTerminated();
		//새로운 할인 적용
		return addDiscount(discount.getSubscription().getSubId(),discountId); //생성된 할인 테이블 아이디 반환
	}
}
