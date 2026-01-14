package com.project.core.service;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.plan.Plan;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.core.infra.repository.plan.PlanRepository;
import com.project.core.infra.repository.plan.SubscriptionPlanRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.core.util.PhoneUtil;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import com.project.global.exception.core.OperationFailedException;
import com.project.global.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

	private final SubscriptionRepository subscriptionRepository;
	private final PlanRepository planRepository;
	private final SubscriptionPlanRepository subscriptionPlanRepository;
	private final CustomerRepository customerRepository;
	private final AESUtil aesUtil;
	private final Clock clock;

	private static int MAX_PHONE_NUMBER_GENERATION_ATTEMPTS = 0;

	/**
	 * 요금제 가입 (신규 개통)
	 * 1. 활성 회선이 0개면 -> Customer의 연락처 사용 시도
	 * 2. 활성 회선이 있거나 위 번호가 이미 사용 중이면 -> 랜덤 번호 생성
	 */
	public void joinSubscription(Long customerId, Long planId) {
		// 고객, 요금제 조회
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

		Plan plan = planRepository.findById(planId)
				.orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.PLAN_NOT_FOUND));

		// 전화번호 결정 로직
		String phoneNumberEnc = determinePhoneNumber(customer);

		// 회선 생성
		Subscription newSub = Subscription.builder()
				.customer(customer)
				.phoneNumber(phoneNumberEnc)
				.clock(clock)
				.build();
		subscriptionRepository.save(newSub);

		// 초기 요금제 이력 생성
		SubscriptionPlan initPlan = SubscriptionPlan.builder()
				.subscription(newSub)
				.plan(plan)
				.build();
		subscriptionPlanRepository.save(initPlan);
	}

	// 번호 결정 메소드
	private String determinePhoneNumber(Customer customer) {
		// 고객의 현재 활성 회선 수 확인 (ACTIVE인 것만)
		long activeCount = subscriptionRepository.countByCustomerAndStatus(customer, SubscriptionStatus.ACTIVE);

		// 활성 회선이 0개라면? -> 쓰던 번호(Customer 연락처) 복구 시도
		if (activeCount == 0) {
			String customerContactEnc = customer.getContactEnc();

			// 중복 체크
			boolean isUsed = subscriptionRepository.existsByPhoneNumberAndStatus(customerContactEnc, SubscriptionStatus.ACTIVE);

			if (!isUsed) {
				return customerContactEnc;
			}
		}

		// 회선이 이미 있거나, 내 번호를 못 쓰는 상황 -> 새 랜덤 번호 채번
		return generateUniqueRandomPhoneNumberEnc();
	}

	// 중복 없는 랜덤 번호 생성 (암호화된 값 반환)
	private String generateUniqueRandomPhoneNumberEnc() {
		String randomPhone;
		String randomPhoneEnc;

		// 무한 루프 방지를 위한 최대 시도 횟수
		MAX_PHONE_NUMBER_GENERATION_ATTEMPTS = 0;

		do {
			if (MAX_PHONE_NUMBER_GENERATION_ATTEMPTS > 10) throw new OperationFailedException(CoreErrorCode.PHONE_NUMBER_GENERATION_FAILED);

			randomPhone = PhoneUtil.generateRandomPhoneNumber();
			try {
				randomPhoneEnc = aesUtil.encrypt(randomPhone);
			} catch (Exception e) {
				throw new OperationFailedException(CoreErrorCode.ENCRYPTION_FAILED);
			}

			MAX_PHONE_NUMBER_GENERATION_ATTEMPTS++;
		} while (subscriptionRepository.existsByPhoneNumber(randomPhoneEnc)); // DB에 이미 있는지(해지된 것 포함) 체크

		return randomPhoneEnc;
	}

	/**
	 * 요금제 변경 (기존 요금제 해지 -> 신규 요금제 가입
	 */
	public void changePlan(Long subId, Long newPlanId) {

		// 회선 존재 여부 확인
		Subscription sub = subscriptionRepository.findById(subId)
				.orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

		// 현재 사용 중인 요금제 찾아서 종료 처리
		subscriptionPlanRepository.findActivePlanBySubId(subId)
				.ifPresent(SubscriptionPlan::expire);

		// 변경할 새 요금제 정보 조회
		Plan newPlan = planRepository.findById(newPlanId)
				.orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.PLAN_NOT_FOUND));

		// 새 요금제 가입 이력 생성 및 저장
		SubscriptionPlan newHistory = SubscriptionPlan.builder()
				.subscription(sub)
				.plan(newPlan)
				.build();

		subscriptionPlanRepository.save(newHistory);
	}

	/**
	 * 요금제 해지 (회선 정지)
	 */
	public void terminateSubscription(Long subId) {
		Subscription sub = subscriptionRepository.findById(subId)
				.orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

		if (sub.getStatus() == SubscriptionStatus.TERMINATED) {
			throw new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
		}

		sub.terminate(clock);

		subscriptionPlanRepository.findActivePlanBySubId(subId)
				.ifPresent(SubscriptionPlan::expire);
	}
}
