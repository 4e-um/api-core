package com.project.core.service;

import com.project.core.controller.dto.response.VasBulkTerminateResponse;
import com.project.core.controller.dto.response.VasJoinResponse;
import com.project.core.controller.dto.response.VasTerminateResponse;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.entity.vas.SubscriptionVas;
import com.project.core.infra.entity.vas.Vas;
import com.project.core.infra.entity.vas.enums.VasStatus;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.core.infra.repository.vas.SubscriptionVasRepository;
import com.project.core.infra.repository.vas.VasRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VasService {

  private final SubscriptionRepository subscriptionRepository;
  private final VasRepository vasRepository;
  private final SubscriptionVasRepository subscriptionVasRepository;
  private final Clock clock;

  // 부가서비스 가입
  public VasJoinResponse joinVas(Long subId, Long vasId) {
    // 회선 조회 및 활성 상태 체크
    Subscription subscription =
        subscriptionRepository
            .findById(subId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

    if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    // 부가서비스 상품 조회
    Vas vas =
        vasRepository
            .findById(vasId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.VAS_NOT_FOUND));

    // 이미 가입되어 있는지 확인
    boolean isAlreadyJoined =
        subscriptionVasRepository.existsBySubscriptionSubIdAndVasVasIdAndStatus(
            subId, vasId, VasStatus.ACTIVE);

    if (isAlreadyJoined) {
      throw new InvalidStateException(CoreErrorCode.VAS_ALREADY_SUBSCRIBED);
    }

    // 가입 처리
    SubscriptionVas newSubscriptionVas =
        SubscriptionVas.builder().subscription(subscription).vas(vas).clock(clock).build();
    SubscriptionVas savedVas = subscriptionVasRepository.save(newSubscriptionVas);

    return new VasJoinResponse(
        savedVas.getSvId(), subId, vasId, savedVas.getStatus().name(), savedVas.getStartDate());
  }

  // 부가서비스 해지
  public VasTerminateResponse terminateVas(Long subId, Long vasId) {
    // 회선 조회 및 활성 상태 체크
    Subscription subscription =
        subscriptionRepository
            .findById(subId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

    if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    // 해지할 활성 부가서비스 찾기
    SubscriptionVas subscriptionVas =
        subscriptionVasRepository
            .findBySubscriptionSubIdAndVasVasIdAndStatus(subId, vasId, VasStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.VAS_ALREADY_TERMINATED));

    // 해지 처리
    subscriptionVas.terminate(clock);

    return new VasTerminateResponse(
        subscriptionVas.getSvId(),
        subId,
        vasId,
        subscriptionVas.getStatus().name(),
        subscriptionVas.getEndDate());
  }

  // 부가서비스 일괄 해지
  public VasBulkTerminateResponse terminateVasBulk(Long subId, List<Long> vasIds) {
    // 회선 조회 및 활성 상태 체크
    Subscription subscription =
        subscriptionRepository
            .findById(subId)
            .orElseThrow(() -> new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

    if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    // 해지 대상 조회 (Active 상태이면서 & 요청된 ID 목록에 있는 것들)
    List<SubscriptionVas> targetList =
        subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdInAndStatus(
            subId, vasIds, VasStatus.ACTIVE);

    if (targetList.isEmpty()) {
      throw new EntityNotFoundException(CoreErrorCode.VAS_NOT_FOUND);
    }

    // 일괄 해지 처리 및 결과 변환
    List<VasTerminateResponse> responses =
        targetList.stream()
            .map(
                subVas -> {
                  // 해지 처리 (시간 설정 및 상태 변경)
                  subVas.terminate(clock);

                  return new VasTerminateResponse(
                      subVas.getSvId(),
                      subId,
                      subVas.getVas().getVasId(),
                      subVas.getStatus().name(),
                      subVas.getEndDate());
                })
            .toList();

    return new VasBulkTerminateResponse(subId, responses.size(), responses);
  }
}
