package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import java.time.Instant;
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
    // given
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
        .willReturn(false);

    SubscriptionVas savedVas =
        SubscriptionVas.builder().subscription(subscription).vas(vas).clock(clock).build();
    ReflectionTestUtils.setField(savedVas, "svId", 100L);
    given(subscriptionVasRepository.save(any(SubscriptionVas.class))).willReturn(savedVas);

    // when
    VasJoinResponse response = vasService.joinVas(subId, vasId);

    // then
    assertThat(response).isNotNull();
    assertThat(response.subVasId()).isEqualTo(100L);
    assertThat(response.status()).isEqualTo("ACTIVE");
    verify(subscriptionVasRepository).save(any(SubscriptionVas.class));
  }

  @Test
  @DisplayName("[가입] 실패 - 회선 없음")
  void joinVasFailSubNotFound() {
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.empty());

    assertThatThrownBy(() -> vasService.joinVas(1L, 1L))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
  }

  @Test
  @DisplayName("[가입] 실패 - 이미 해지된 회선")
  void joinVasFailSubTerminated() {
    Subscription subscription = mock(Subscription.class);
    given(subscription.getStatus()).willReturn(SubscriptionStatus.TERMINATED);
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(subscription));

    assertThatThrownBy(() -> vasService.joinVas(1L, 1L))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
  }

  @Test
  @DisplayName("[가입] 실패 - 부가서비스 정보 없음")
  void joinVasFailVasNotFound() {
    Subscription subscription = mock(Subscription.class);
    given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(subscription));

    given(vasRepository.findById(any(Long.class))).willReturn(Optional.empty());

    assertThatThrownBy(() -> vasService.joinVas(1L, 1L))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.VAS_NOT_FOUND);
  }

  @Test
  @DisplayName("[가입] 실패 - 이미 가입된 부가서비스")
  void joinVasFailAlreadySubscribed() {
    Subscription subscription = mock(Subscription.class);
    given(subscription.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(subscription));

    Vas vas = mock(Vas.class);
    given(vasRepository.findById(any(Long.class))).willReturn(Optional.of(vas));

    given(
            subscriptionVasRepository.existsBySubscriptionSubIdAndVasVasIdAndStatus(
                any(Long.class), any(Long.class), eq(VasStatus.ACTIVE)))
        .willReturn(true);

    assertThatThrownBy(() -> vasService.joinVas(1L, 1L))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.VAS_ALREADY_SUBSCRIBED);
  }

  @Test
  @DisplayName("[해지] 성공 - 부가서비스 해지")
  void terminateVasSuccess() {
    Long subId = 1L;
    Long vasId = 1L;

    given(subscriptionRepository.existsById(subId)).willReturn(true);

    SubscriptionVas subVas =
        SubscriptionVas.builder()
            .subscription(mock(Subscription.class))
            .vas(mock(Vas.class))
            .clock(clock)
            .build();
    ReflectionTestUtils.setField(subVas, "svId", 100L);
    given(
            subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdAndStatus(
                subId, vasId, VasStatus.ACTIVE))
        .willReturn(Optional.of(subVas));

    // when
    VasTerminateResponse response = vasService.terminateVas(subId, vasId);

    // then
    assertThat(response.status()).isEqualTo("TERMINATED");
    assertThat(subVas.getStatus()).isEqualTo(VasStatus.TERMINATED);
    assertThat(subVas.getEndDate()).isNotNull();
  }

  @Test
  @DisplayName("[해지] 실패 - 회선 존재하지 않음")
  void terminateVasFailSubNotFound() {
    given(subscriptionRepository.existsById(any(Long.class))).willReturn(false);

    assertThatThrownBy(() -> vasService.terminateVas(1L, 1L))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
  }

  @Test
  @DisplayName("[해지] 실패 - 가입되지 않았거나 이미 해지된 부가서비스")
  void terminateVasFailAlreadyTerminated() {
    Long subId = 1L;
    Long vasId = 1L;

    given(subscriptionRepository.existsById(subId)).willReturn(true);
    given(
            subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdAndStatus(
                subId, vasId, VasStatus.ACTIVE))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> vasService.terminateVas(subId, vasId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.VAS_ALREADY_TERMINATED);
  }

  @Test
  @DisplayName("[일괄해지] 성공 - 부가서비스 일괄 해지")
  void terminateVasBulkSuccess() {
    Long subId = 1L;
    List<Long> vasIds = List.of(1L, 2L);

    given(subscriptionRepository.existsById(subId)).willReturn(true);

    Vas vas1 = mock(Vas.class);
    given(vas1.getVasId()).willReturn(1L);
    Vas vas2 = mock(Vas.class);
    given(vas2.getVasId()).willReturn(2L);

    SubscriptionVas sv1 =
        SubscriptionVas.builder()
            .subscription(mock(Subscription.class))
            .vas(vas1)
            .clock(clock)
            .build();
    SubscriptionVas sv2 =
        SubscriptionVas.builder()
            .subscription(mock(Subscription.class))
            .vas(vas2)
            .clock(clock)
            .build();

    given(
            subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdInAndStatus(
                subId, vasIds, VasStatus.ACTIVE))
        .willReturn(List.of(sv1, sv2));

    // when
    VasBulkTerminateResponse response = vasService.terminateVasBulk(subId, vasIds);

    // then
    assertThat(response.count()).isEqualTo(2);
    assertThat(sv1.getStatus()).isEqualTo(VasStatus.TERMINATED);
    assertThat(sv2.getStatus()).isEqualTo(VasStatus.TERMINATED);
  }

  @Test
  @DisplayName("[일괄해지] 실패 - 회선 존재하지 않음")
  void terminateVasBulkFailSubNotFound() {
    given(subscriptionRepository.existsById(any(Long.class))).willReturn(false);

    assertThatThrownBy(() -> vasService.terminateVasBulk(1L, List.of(1L)))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
  }

  @Test
  @DisplayName("[일괄해지] 실패 - 해지할 부가서비스 없음")
  void terminateVasBulkFailVasNotFound() {
    Long subId = 1L;
    List<Long> vasIds = List.of(1L, 2L);

    given(subscriptionRepository.existsById(subId)).willReturn(true);
    given(
            subscriptionVasRepository.findBySubscriptionSubIdAndVasVasIdInAndStatus(
                subId, vasIds, VasStatus.ACTIVE))
        .willReturn(Collections.emptyList());

    assertThatThrownBy(() -> vasService.terminateVasBulk(subId, vasIds))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.VAS_NOT_FOUND);
  }
}
