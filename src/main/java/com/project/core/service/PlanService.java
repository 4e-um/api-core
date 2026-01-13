package com.project.core.service;

import com.project.core.infra.entity.plan.Plan;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.plan.PlanRepository;
import com.project.core.infra.repository.plan.SubscriptionPlanRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

	private final SubscriptionRepository subscriptionRepository;
	private final PlanRepository planRepository;
	private final SubscriptionPlanRepository subscriptionPlanRepository;

	/**
	 * 요금제 변경 (기존 요금제 해지 -> 신규 요금제 가입
	 */
	public void changePlan(Long subId, Long newPlanId) {

		// 회선 존재 여부 확인
		Subscription sub = subscriptionRepository.findById(subId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회선입니다."));

		// 현재 사용 중인 요금제 찾아서 종료 처리
		subscriptionPlanRepository.findActivePlanBySubId(subId)
				.ifPresent(SubscriptionPlan::expire);

		// 변경할 새 요금제 정보 조회
		Plan newPlan = planRepository.findById(newPlanId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제입니다."));

		// 새 요금제 가입 이력 생성 및 저장
		SubscriptionPlan newHistory = SubscriptionPlan.builder()
				.subscription(sub)
				.plan(newPlan)
				.build();

		subscriptionPlanRepository.save(newHistory);
	}
}
