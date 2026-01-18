package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.core.controller.dto.VasDto;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VasServiceTest {

    @InjectMocks private VasService vasService;

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private VasRepository vasRepository;
    @Mock private SubscriptionVasRepository subscriptionVasRepository;
    @Mock private Clock clock;

    @BeforeEach
    void setup() {
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        given(clock.instant()).willReturn(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("[가입] 성공 - 부가서비스 가입")
    void joinVasSuccess() {
        Long subId = 1L;

        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        Long vasId = 1L;
        Vas vas = mock(Vas.class);
        given(vas.getMonthlyFee()).willReturn(1000);
        given(vasRepository.findById(vasId)).willReturn(Optional.of(vas));

        given(
                        subscriptionVasRepository.existsBySubscriptionSubIdAndVasVasIdAndStatus(
                                any(), any(), eq(VasStatus.ACTIVE)))
                .willReturn(false);

        SubscriptionVas savedVas =
                SubscriptionVas.builder().subscription(subscription).vas(vas).clock(clock).build();
        ReflectionTestUtils.setField(savedVas, "svId", 100L);
        given(subscriptionVasRepository.save(any(SubscriptionVas.class))).willReturn(savedVas);

        VasDto.JoinResponse response = vasService.joinVas(subId, vasId);

        assertThat(response).isNotNull();
        assertThat(response.subVasId()).isEqualTo(100L);
        verify(subscriptionVasRepository).save(any(SubscriptionVas.class));
    }

    @Test
    @DisplayName("[가입] 실패 - 회선 없음")
    void joinVasFailSubNotFound() {
        given(subscriptionRepository.findById(any())).willReturn(Optional.empty());

        EntityNotFoundException ex =
                assertThrows(EntityNotFoundException.class, () -> vasService.joinVas(1L, 1L));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("[가입] 실패 - 이미 해지된 회선")
    void joinVasFailSubTerminated() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.TERMINATED);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        InvalidStateException ex =
                assertThrows(InvalidStateException.class, () -> vasService.joinVas(subId, 1L));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("[가입] 실패 - 정지된 회선")
    void joinVasFailSubSuspended() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.SUSPENDED);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        InvalidStateException ex =
                assertThrows(InvalidStateException.class, () -> vasService.joinVas(subId, 1L));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_SUSPENDED);
    }

    @Test
    @DisplayName("[가입] 실패 - 부가서비스 정보 없음")
    void joinVasFailVasNotFound() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        given(vasRepository.findById(any())).willReturn(Optional.empty());

        EntityNotFoundException ex =
                assertThrows(EntityNotFoundException.class, () -> vasService.joinVas(subId, 1L));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.VAS_NOT_FOUND);
    }

    @Test
    @DisplayName("[가입] 실패 - 이미 가입된 부가서비스")
    void joinVasFailAlreadySubscribed() {
        Long subId = 1L;
        Long vasId = 1L;

        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        Vas vas = mock(Vas.class);
        given(vasRepository.findById(vasId)).willReturn(Optional.of(vas));

        given(
                        subscriptionVasRepository.existsBySubscriptionSubIdAndVasVasIdAndStatus(
                                subId, vasId, VasStatus.ACTIVE))
                .willReturn(true);

        InvalidStateException ex =
                assertThrows(InvalidStateException.class, () -> vasService.joinVas(subId, vasId));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.VAS_ALREADY_SUBSCRIBED);
    }

    @Test
    @DisplayName("[해지] 성공 - 부가서비스 해지")
    void terminateVasSuccess() {
        Long subId = 1L;

        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        Vas vas = mock(Vas.class);
        given(vas.getMonthlyFee()).willReturn(1000);
        SubscriptionVas subVas =
                SubscriptionVas.builder().subscription(subscription).vas(vas).clock(clock).build();
        ReflectionTestUtils.setField(subVas, "svId", 100L);

        given(
                        subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdAndStatus(
                                any(), any(), eq(VasStatus.ACTIVE)))
                .willReturn(Optional.of(subVas));

        Long vasId = 1L;
        VasDto.TerminateResponse response = vasService.terminateVas(subId, vasId);

        assertThat(response.status()).isEqualTo("TERMINATED");
        assertThat(subVas.getStatus()).isEqualTo(VasStatus.TERMINATED);
    }

    @Test
    @DisplayName("[해지] 실패 - 회선 없음")
    void terminateVasFailSubNotFound() {
        given(subscriptionRepository.findById(any())).willReturn(Optional.empty());

        EntityNotFoundException ex =
                assertThrows(EntityNotFoundException.class, () -> vasService.terminateVas(1L, 1L));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("[해지] 실패 - 이미 해지된 회선")
    void terminateVasFailSubTerminated() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.TERMINATED);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        InvalidStateException ex =
                assertThrows(InvalidStateException.class, () -> vasService.terminateVas(subId, 1L));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("[해지] 실패 - 정지된 회선")
    void terminateVasFailSubSuspended() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.SUSPENDED);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        InvalidStateException ex =
                assertThrows(InvalidStateException.class, () -> vasService.terminateVas(subId, 1L));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_SUSPENDED);
    }

    @Test
    @DisplayName("[해지] 실패 - 가입되지 않았거나 이미 해지된 부가서비스")
    void terminateVasFailAlreadyTerminated() {
        Long subId = 1L;

        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        given(
                        subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdAndStatus(
                                any(), any(), eq(VasStatus.ACTIVE)))
                .willReturn(Optional.empty());

        Long vasId = 1L;
        EntityNotFoundException ex =
                assertThrows(
                        EntityNotFoundException.class, () -> vasService.terminateVas(subId, vasId));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.VAS_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("[일괄해지] 성공 - 부가서비스 일괄 해지")
    void terminateVasBulkSuccess() {
        Long subId = 1L;

        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        Vas v1 = mock(Vas.class);
        given(v1.getMonthlyFee()).willReturn(1000);
        given(v1.getVasId()).willReturn(1L);
        Vas v2 = mock(Vas.class);
        given(v2.getMonthlyFee()).willReturn(2000);
        given(v2.getVasId()).willReturn(2L);

        SubscriptionVas sv1 =
                SubscriptionVas.builder().subscription(subscription).vas(v1).clock(clock).build();
        SubscriptionVas sv2 =
                SubscriptionVas.builder().subscription(subscription).vas(v2).clock(clock).build();

        given(
                        subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdInAndStatus(
                                any(), anyList(), eq(VasStatus.ACTIVE)))
                .willReturn(List.of(sv1, sv2));

        List<Long> vasIds = List.of(1L, 2L);
        VasDto.BulkTerminateResponse response = vasService.terminateVasBulk(subId, vasIds);

        assertThat(response.count()).isEqualTo(2);
        assertThat(sv1.getStatus()).isEqualTo(VasStatus.TERMINATED);
        assertThat(sv2.getStatus()).isEqualTo(VasStatus.TERMINATED);
    }

    @Test
    @DisplayName("[일괄해지] 실패 - 이미 해지된 회선")
    void terminateVasBulkFailSubTerminated() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.TERMINATED);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        List<Long> vasIds = List.of(1L);
        InvalidStateException ex =
                assertThrows(
                        InvalidStateException.class,
                        () -> vasService.terminateVasBulk(subId, vasIds));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("[일괄해지] 실패 - 정지된 회선")
    void terminateVasBulkFailSubSuspended() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.SUSPENDED);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        List<Long> vasIds = List.of(1L);
        InvalidStateException ex =
                assertThrows(
                        InvalidStateException.class,
                        () -> vasService.terminateVasBulk(subId, vasIds));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_SUSPENDED);
    }

    @Test
    @DisplayName("[일괄해지] 실패 - 회선 존재하지 않음")
    void terminateVasBulkFailSubNotFound() {
        given(subscriptionRepository.findById(any())).willReturn(Optional.empty());

        List<Long> vasIds = List.of(1L);
        EntityNotFoundException ex =
                assertThrows(
                        EntityNotFoundException.class,
                        () -> vasService.terminateVasBulk(1L, vasIds));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("[일괄해지] 실패 - 해지할 부가서비스 없음")
    void terminateVasBulkFailVasNotFound() {
        Long subId = 1L;
        Subscription subscription = mock(Subscription.class);
        given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        given(
                        subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdInAndStatus(
                                any(), anyList(), eq(VasStatus.ACTIVE)))
                .willReturn(Collections.emptyList());

        List<Long> vasIds = List.of(1L);
        EntityNotFoundException ex =
                assertThrows(
                        EntityNotFoundException.class,
                        () -> vasService.terminateVasBulk(subId, vasIds));
        assertThat(ex.getCode()).isEqualTo(CoreErrorCode.VAS_NOT_FOUND);
    }

    @Test
    @DisplayName("[조회] 성공 - 부가서비스 이력 조회")
    void getVasHistorySuccess() {
        // given
        Long subId = 1L;
        Vas vas1 = mock(Vas.class);
        given(vas1.getName()).willReturn("Vas A");
        SubscriptionVas sv1 = mock(SubscriptionVas.class);
        given(sv1.getSvId()).willReturn(10L);
        given(sv1.getVas()).willReturn(vas1);
        given(sv1.getMonthlyFee()).willReturn(1000);
        given(sv1.getStatus()).willReturn(VasStatus.TERMINATED);
        given(sv1.getStartDate()).willReturn(LocalDateTime.now().minusDays(30));
        given(sv1.getEndDate()).willReturn(LocalDateTime.now());

        Vas vas2 = mock(Vas.class);
        given(vas2.getName()).willReturn("Vas B");
        SubscriptionVas sv2 = mock(SubscriptionVas.class);
        given(sv2.getSvId()).willReturn(11L);
        given(sv2.getVas()).willReturn(vas2);
        given(sv2.getMonthlyFee()).willReturn(2000);
        given(sv2.getStatus()).willReturn(VasStatus.ACTIVE);
        given(sv2.getStartDate()).willReturn(LocalDateTime.now());

        given(subscriptionVasRepository.findBySubscriptionSubIdOrderByStartDateDesc(subId))
                .willReturn(java.util.List.of(sv2, sv1));

        // when
        java.util.List<VasDto.HistoryResponse> result = vasService.getVasHistory(subId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).vasName()).isEqualTo("Vas B");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        assertThat(result.get(1).vasName()).isEqualTo("Vas A");
        assertThat(result.get(1).status()).isEqualTo("TERMINATED");
    }
}
