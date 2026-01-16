package com.project.core.service;

import com.project.core.controller.dto.response.PlanChangeResponse;
import com.project.core.controller.dto.response.SubscriptionJoinResponse;
import com.project.core.controller.dto.response.SubscriptionPlanResponse;
import com.project.core.controller.dto.response.SubscriptionTerminateResponse;
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
import com.project.global.util.AesUtil;
import java.time.Clock;
import java.util.List;

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
    private final CustomerRepository customerRepository;
    private final AesUtil aesUtil;
    private final Clock clock;

    private static final int PHONE_NUMBER_GENERATION_ATTEMPT_LIMIT = 11;

  /**
   * 요금제 변경 이력 조회
   */
  @Transactional(readOnly = true)
  public List<SubscriptionPlanResponse> getPlanHistory(Long subId) {

    return subscriptionPlanRepository.findBySubscriptionSubIdOrderByCreatedDateDesc(subId).stream()
            .map(sp -> new SubscriptionPlanResponse(
                    sp.getSpId(),
                    sp.getPlan().getPlanName(),
                    sp.getCost(),
                    sp.getCreatedDate(),
                    sp.getLeftDate()
            ))
            .toList();
  }

  /**
   * 요금제 가입 (신규 개통) 1. 활성 회선이 0개면 -> Customer의 연락처 사용 시도 2. 활성 회선이 있거나 위 번호가 이미 사용 중이면 -> 랜덤 번호 생성
   */
  public SubscriptionJoinResponse joinSubscription(Long customerId, Long planId) {
    // 고객, 요금제 조회
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

        Plan plan =
                planRepository
                        .findById(planId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(CoreErrorCode.PLAN_NOT_FOUND));

        // 전화번호 결정 로직
        String phoneNumberEnc = determinePhoneNumber(customer);

        // 회선 생성
        Subscription newSub =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber(phoneNumberEnc)
                        .clock(clock)
                        .build();
        subscriptionRepository.save(newSub);

        // 초기 요금제 이력 생성
        SubscriptionPlan initPlan =
                SubscriptionPlan.builder().subscription(newSub).plan(plan).build();
        subscriptionPlanRepository.save(initPlan);

        return SubscriptionJoinResponse.builder()
                .subId(newSub.getSubId())
                .customerId(customer.getCustomerId())
                .planId(plan.getPlanId())
                .phoneNumber(aesUtil.decrypt(newSub.getPhoneNumber()))
                .status(newSub.getStatus())
                .startDate(newSub.getStartDate())
                .build();
    }

    // 번호 결정 메소드
    private String determinePhoneNumber(Customer customer) {
        // 고객의 현재 활성 회선 수 확인 (ACTIVE인 것만)
        long activeCount =
                subscriptionRepository.countByCustomerAndStatus(
                        customer, SubscriptionStatus.ACTIVE);

        // 활성 회선이 0개라면? -> 쓰던 번호(Customer 연락처) 복구 시도
        if (activeCount == 0) {
            String customerContactEnc = customer.getContactEnc();

            // 중복 체크
            boolean isUsed =
                    subscriptionRepository.existsByPhoneNumberAndStatus(
                            customerContactEnc, SubscriptionStatus.ACTIVE);

            if (!isUsed) {
                return customerContactEnc;
            }
        }

        // 회선이 이미 있거나, 내 번호를 못 쓰는 상황 -> 새 랜덤 번호 채번
        return generateUniqueRandomPhoneNumberEnc();
    }

    // 중복 없는 랜덤 번호 생성 (암호화된 값 반환)
    private String generateUniqueRandomPhoneNumberEnc() {
        for (int i = 0; i < PHONE_NUMBER_GENERATION_ATTEMPT_LIMIT; i++) {
            String randomPhone = PhoneUtil.generateRandomPhoneNumber();
            String randomPhoneEnc = aesUtil.encrypt(randomPhone);
            if (!subscriptionRepository.existsByPhoneNumber(randomPhoneEnc)) {
                return randomPhoneEnc;
            }
        }
        throw new OperationFailedException(CoreErrorCode.PHONE_NUMBER_GENERATION_FAILED);
    }

    /** 요금제 변경 (기존 요금제 해지 -> 신규 요금제 가입 */
    public PlanChangeResponse changePlan(Long subId, Long newPlanId) {

    // 회선 존재 여부 확인
    Subscription sub = findActiveSubscription(subId);

        // 변경하려는 요금제가 현재 요금제와 동일한지 확인
        if (subscriptionPlanRepository
                .findActivePlanBySubId(subId)
                .map(sp -> sp.getPlan().getPlanId())
                .filter(newPlanId::equals)
                .isPresent()) {
            throw new InvalidStateException(CoreErrorCode.PLAN_ALREADY_SUBSCRIBED);
        }

        // 현재 사용 중인 요금제 찾아서 종료 처리
        SubscriptionPlan currentPlan =
                subscriptionPlanRepository
                        .findActivePlanBySubId(subId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(CoreErrorCode.PLAN_NOT_FOUND));
        currentPlan.expire();

        // 변경할 새 요금제 정보 조회
        Plan newPlan =
                planRepository
                        .findById(newPlanId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(CoreErrorCode.PLAN_NOT_FOUND));

        // 새 요금제 가입 이력 생성 및 저장
        SubscriptionPlan newHistory =
                SubscriptionPlan.builder().subscription(sub).plan(newPlan).build();

        subscriptionPlanRepository.save(newHistory);

        return PlanChangeResponse.builder()
                .subId(sub.getSubId())
                .oldPlanId(currentPlan.getPlan().getPlanId())
                .newPlanId(newPlan.getPlanId())
                .changedAt(newHistory.getCreatedDate())
                .build();
    }

  /** 요금제 해지 (회선 정지) */
  public SubscriptionTerminateResponse terminateSubscription(Long subId) {
    Subscription sub = findActiveSubscription(subId);

        if (sub.getStatus() == SubscriptionStatus.TERMINATED) {
            throw new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
        }

        sub.terminate(clock);

        subscriptionPlanRepository.findActivePlanBySubId(subId).ifPresent(SubscriptionPlan::expire);

    return SubscriptionTerminateResponse.builder()
        .subId(sub.getSubId())
        .status(sub.getStatus())
        .terminatedAt(sub.getEndDate())
        .build();
  }

  private Subscription findActiveSubscription(Long subId) {
    Subscription subscription =
            subscriptionRepository
                    .findById(subId)
                    .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));
    if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }
    return subscription;
  }
}
