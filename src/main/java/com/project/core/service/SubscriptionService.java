package com.project.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.response.SubscriptionDetailResponse;
import com.project.core.controller.dto.response.SubscriptionListResponse;
import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.plan.enums.AllotmentPeriod;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.usage.UsageSummaryDaily;
import com.project.core.infra.entity.usage.UsageSummaryMonthly;
import com.project.core.infra.repository.plan.SubscriptionPlanRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.core.infra.repository.usage.UsageSummaryDailyRepository;
import com.project.core.infra.repository.usage.UsageSummaryMonthlyRepository;
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
    private final UsageSummaryDailyRepository usageSummaryDailyRepository;
    private final UsageSummaryMonthlyRepository usageSummaryMonthlyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AesUtil aesUtil;
    private final ContactHashUtil contactHashUtil;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    /** 전체 회선 목록 조회 (슬라이스) */
    @Transactional(readOnly = true)
    public Slice<SubscriptionListResponse> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository
                .findAllSlice(pageable)
                .map(
                        sub -> {
                            String decryptedEmail = safeDecrypt(sub.getCustomer().getEmailEnc());
                            String decryptedPhone = safeDecrypt(sub.getPhoneNumber());

                            SubscriptionPlan subPlan =
                                    subscriptionPlanRepository
                                            .findActivePlanBySubId(sub.getSubId())
                                            .orElseThrow(
                                                    () ->
                                                            new EntityNotFoundException(
                                                                    CoreErrorCode
                                                                            .SUBSCRIPTION_NOT_FOUND));

                            long totalUsedBytes =
                                    getSubscriptionTotalAmount(subPlan) / (1024 * 1024);
                            return SubscriptionListResponse.of(
                                    sub,
                                    totalUsedBytes,
                                    subPlan.getAllotmentAmount(),
                                    decryptedEmail,
                                    decryptedPhone);
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

        SubscriptionPlan subPlan =
                subscriptionPlanRepository
                        .findActivePlanBySubId(sub.getSubId())
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

        long totalUsedBytes = getSubscriptionTotalAmount(subPlan) / (1024 * 1024);

        // SubscriptionListResponse의 of 로직을 활용하여 기본 정보 추출
        SubscriptionListResponse baseInfo =
                SubscriptionListResponse.of(
                        sub,
                        totalUsedBytes,
                        subPlan.getAllotmentAmount(),
                        decryptedEmail,
                        decryptedPhone);

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

    private Long getSubscriptionTotalAmount(SubscriptionPlan subPlan) {

        if (subPlan.getAllotmentPeriod() == AllotmentPeriod.DAY) {
            return getDailyTotalAmount(subPlan.getSubscription().getSubId());
        }

        return getMonthlyTotalAmount(subPlan.getSubscription().getSubId());
    }

    private Long getDailyTotalAmount(Long subId) {
        String usageDate =
                LocalDate.now(ZoneId.of("Asia/Seoul"))
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return usageSummaryDailyRepository
                .findBySubIdAndUsageDate(subId, usageDate)
                .map(UsageSummaryDaily::getTotalUsedBytes)
                .orElse(0L);
    }

    private Long getMonthlyTotalAmount(Long subId) {
        String period = LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(MONTH_FMT);

        return usageSummaryMonthlyRepository
                .findBySubIdAndPeriod(subId, period)
                .map(UsageSummaryMonthly::getTotalUsedBytes)
                .orElse(0L);
    }
}
