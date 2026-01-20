package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.controller.dto.response.CustomerListResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.customer.enums.Grade;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.global.exception.code.domain.GlobalErrorCode;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import com.project.global.exception.core.OperationFailedException;
import com.project.global.util.AesUtil;
import com.project.global.util.ContactHashUtil;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @InjectMocks private CustomerService customerService;

    @Mock private CustomerRepository customerRepository;
    @Mock private AesUtil aesUtil;
    @Mock private ContactHashUtil contactHashUtil;

    @Test
    @DisplayName("[조회] 성공 - 검색어가 공백(Blank)일 때 전체 조회 수행")
    void getAllCustomers_BlankSearch() {
        // given
        String search = "   ";
        Pageable pageable = Pageable.unpaged();

        given(customerRepository.findAll(pageable))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        // when
        customerService.getAllCustomers(search, pageable);

        // then
        verify(customerRepository).findAll(pageable);
        verifyNoInteractions(contactHashUtil);
    }

    @Test
    @DisplayName("[조회] 성공 - ACTIVE 구독이 여러 개일 때 가장 최근 시작된 구독 선택")
    void convertToDto_PickLatestActiveSubscription() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("다중구독")
                        .grade(Grade.GENERAL)
                        .contactEnc("enc")
                        .contactHash("hash")
                        .emailEnc("enc")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 10L);

        LocalDateTime now = LocalDateTime.now();

        Subscription oldActive =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber("phone1")
                        .clock(Clock.systemDefaultZone())
                        .build();
        ReflectionTestUtils.setField(oldActive, "startDate", now.minusDays(10));
        ReflectionTestUtils.setField(oldActive, "subId", 101L);
        ReflectionTestUtils.setField(oldActive, "status", SubscriptionStatus.ACTIVE);

        Subscription latestActive =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber("phone2")
                        .clock(Clock.systemDefaultZone())
                        .build();
        ReflectionTestUtils.setField(latestActive, "startDate", now);
        ReflectionTestUtils.setField(latestActive, "subId", 102L);
        ReflectionTestUtils.setField(latestActive, "status", SubscriptionStatus.ACTIVE);

        customer.getSubscriptionHistory().addAll(List.of(oldActive, latestActive));

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt(any())).willReturn("010-1111-2222");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        CustomerListResponse dto = result.getContent().get(0);
        assertThat(dto.representativeSubId()).isEqualTo("SUB-0000102");
    }

    @Test
    @DisplayName("[조회] 성공 - ACTIVE가 없고 해지된 구독만 있을 때 대표 회선 없음(NONE) 반환")
    void convertToDto_PickLatestInactiveSubscription() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("해지고객")
                        .grade(Grade.GENERAL)
                        .contactEnc("enc")
                        .contactHash("hash")
                        .emailEnc("enc")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 11L);

        Subscription latestTerminated =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber("phone2")
                        .clock(Clock.systemDefaultZone())
                        .build();
        ReflectionTestUtils.setField(latestTerminated, "status", SubscriptionStatus.TERMINATED);
        ReflectionTestUtils.setField(latestTerminated, "subId", 202L);

        customer.getSubscriptionHistory().add(latestTerminated);

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt("enc")).willReturn("010-3333-4444");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        CustomerListResponse dto = result.getContent().get(0);
        // 메인 코드 로직 상 ACTIVE만 필터링하므로 TERMINATED만 있으면 sub는 null임
        assertThat(dto.subStatus()).isEqualTo("NONE");
        assertThat(dto.representativePhone()).isEqualTo("N/A");
    }

    @Test
    @DisplayName("[조회] 성공 - ACTIVE 구독이 여러 개이고 시작일이 같을 때도 에러 없이 하나 선택")
    void convertToDto_PickActiveSubscription_SameDate() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("동일날짜")
                        .grade(Grade.GENERAL)
                        .contactEnc("enc")
                        .contactHash("hash")
                        .emailEnc("enc")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 12L);

        LocalDateTime now = LocalDateTime.now();

        Subscription sub1 =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber("p1")
                        .clock(Clock.systemDefaultZone())
                        .build();
        ReflectionTestUtils.setField(sub1, "startDate", now);
        ReflectionTestUtils.setField(sub1, "subId", 301L);
        ReflectionTestUtils.setField(sub1, "status", SubscriptionStatus.ACTIVE);

        Subscription sub2 =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber("p2")
                        .clock(Clock.systemDefaultZone())
                        .build();
        ReflectionTestUtils.setField(sub2, "startDate", now);
        ReflectionTestUtils.setField(sub2, "subId", 302L);
        ReflectionTestUtils.setField(sub2, "status", SubscriptionStatus.ACTIVE);

        customer.getSubscriptionHistory().addAll(List.of(sub1, sub2));

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt(any())).willReturn("010-1111-2222");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        assertThat(result).isNotEmpty();
        // 정렬 순서가 같으므로 둘 중 하나가 나옴. 에러가 안 나는지 확인하고, 로직상 먼저 들어간게 나올지 나중게 나올지는 스트림 구현에 따름
        // 여기서는 NotNull인지와 ID가 존재하는지만 확인
        CustomerListResponse dto = result.getContent().get(0);
        assertThat(dto.representativeSubId()).isIn("SUB-0000301", "SUB-0000302");
    }

    @Test
    @DisplayName("[조회] 성공 - 검색 결과 없음")
    void getAllCustomers_SearchNotFound() {
        // given
        String search = "010-9999-9999";
        String hash = "hash-not-found";
        Pageable pageable = Pageable.unpaged();

        given(contactHashUtil.hmacSha256Base64(search)).willReturn(hash);
        given(customerRepository.findByContactHash(hash)).willReturn(Optional.empty());

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(search, pageable);

        // then
        assertThat(result).isEmpty();
        verify(contactHashUtil).hmacSha256Base64(search);
        verify(customerRepository).findByContactHash(hash);
    }

    @Test
    @DisplayName("[조회] 성공 - 비활성(해지) 구독만 있는 경우 대표 회선 없음 확인")
    void getAllCustomers_InactiveSubscriptionOnly() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("해지고객")
                        .grade(Grade.GENERAL)
                        .contactEnc("enc-phone")
                        .contactHash("hash")
                        .emailEnc("enc-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 4L);

        Subscription subTerminated =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber("enc-sub-phone")
                        .clock(Clock.systemDefaultZone())
                        .build();
        ReflectionTestUtils.setField(subTerminated, "status", SubscriptionStatus.TERMINATED);
        ReflectionTestUtils.setField(subTerminated, "subId", 100L);

        customer.getSubscriptionHistory().add(subTerminated);

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt("enc-phone")).willReturn("010-1234-5678");
        given(aesUtil.decrypt("enc-email")).willReturn("term@example.com");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        assertThat(result).isNotEmpty();
        CustomerListResponse dto = result.getContent().get(0);
        assertThat(dto.subStatus()).isEqualTo("NONE");
        assertThat(dto.representativePhone()).isEqualTo("N/A");
    }

    @Test
    @DisplayName("[조회] 성공 - 암호화 필드가 Null일 때 SafeDecrypt 동작 확인")
    void getAllCustomers_NullEncryptedField() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("Null고객")
                        .grade(Grade.GENERAL)
                        .contactEnc("dummy")
                        .contactHash("hash")
                        .emailEnc("enc-email")
                        .build();
        ReflectionTestUtils.setField(customer, "contactEnc", null);
        ReflectionTestUtils.setField(customer, "customerId", 5L);

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt("enc-email")).willReturn("null@example.com");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        assertThat(result).isNotEmpty();
        CustomerListResponse dto = result.getContent().get(0);
        assertThat(dto.contact()).isNull();
    }

    @Test
    @DisplayName("[조회] 실패 - 검색어 해싱 실패 시 빈 페이지 반환")
    void getAllCustomers_SearchHashingFail() {
        // given
        String search = "invalid-input";
        Pageable pageable = Pageable.unpaged();

        given(contactHashUtil.hmacSha256Base64(search))
                .willThrow(new InvalidStateException(GlobalErrorCode.INTERNAL_SERVER_ERROR));

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(search, pageable);

        // then
        assertThat(result).isEmpty();
        verify(contactHashUtil).hmacSha256Base64(search);
    }

    @Test
    @DisplayName("[조회] 성공 - 구독 이력이 없는 고객 조회 (Dto 변환 확인)")
    void getAllCustomers_NoSubscriptions() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("신규고객")
                        .grade(Grade.GENERAL)
                        .contactEnc("enc-phone")
                        .contactHash("hash")
                        .emailEnc("enc-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 2L);

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt("enc-phone")).willReturn("010-1234-5678");
        given(aesUtil.decrypt("enc-email")).willReturn("new@example.com");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        assertThat(result).isNotEmpty();
        CustomerListResponse dto = result.getContent().get(0);
        assertThat(dto.name()).isEqualTo("신규고객");
        assertThat(dto.representativePhone()).isEqualTo("N/A");
        assertThat(dto.subStatus()).isEqualTo("NONE");
    }

    @Test
    @DisplayName("[조회] 성공 - 복호화 실패 시 원본 반환 (SafeDecrypt 확인)")
    void getAllCustomers_DecryptionFail() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("오류고객")
                        .grade(Grade.GENERAL)
                        .contactEnc("corrupted-data")
                        .contactHash("hash")
                        .emailEnc("enc-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 3L);

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt("corrupted-data"))
                .willThrow(new OperationFailedException(CoreErrorCode.ENCRYPTION_FAILED));
        given(aesUtil.decrypt("enc-email")).willReturn("ok@example.com");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        assertThat(result).isNotEmpty();
        CustomerListResponse dto = result.getContent().get(0);
        assertThat(dto.contact()).isEqualTo("***");
    }

    @Test
    @DisplayName("[조회] 성공 - 전체 고객 조회")
    void getAllCustomersSuccess() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash")
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 1L);

        given(customerRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(aesUtil.decrypt("encrypted-phone")).willReturn("010-1234-5678");
        given(aesUtil.decrypt("encrypted-email")).willReturn("test@example.com");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(null, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("홍길동");
        assertThat(result.getContent().get(0).contact()).isEqualTo("010-**34-**78");

        verify(customerRepository).findAll(pageable);
    }

    @Test
    @DisplayName("[조회] 성공 - 검색어로 고객 조회")
    void getAllCustomersWithSearchSuccess() {
        // given
        String search = "010-1234-5678";
        String hash = "hash-value";
        Pageable pageable = Pageable.unpaged();

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash(hash)
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 1L);

        given(contactHashUtil.hmacSha256Base64(search)).willReturn(hash);
        given(customerRepository.findByContactHash(hash)).willReturn(Optional.of(customer));
        given(aesUtil.decrypt("encrypted-phone")).willReturn("010-1234-5678");
        given(aesUtil.decrypt("encrypted-email")).willReturn("test@example.com");

        // when
        Page<CustomerListResponse> result = customerService.getAllCustomers(search, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("홍길동");

        verify(contactHashUtil).hmacSha256Base64(search);
        verify(customerRepository).findByContactHash(hash);
    }

    @Test
    @DisplayName("[조회] 성공 - 고객 상세 조회")
    void getCustomerDetailSuccess() {
        // given
        Long customerId = 1L;
        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash")
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.decrypt("encrypted-phone")).willReturn("010-1234-5678");
        given(aesUtil.decrypt("encrypted-email")).willReturn("test@example.com");

        // when
        CustomerListResponse result = customerService.getCustomerDetail(customerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.contact()).isEqualTo("010-**34-**78");

        verify(customerRepository).findById(customerId);
    }

    @Test
    @DisplayName("[조회] 실패 - 존재하지 않는 고객 상세 조회")
    void getCustomerDetailFailNotFound() {
        // given
        Long customerId = 999L;
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.getCustomerDetail(customerId))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerRepository).findById(customerId);
    }

    @Test
    @DisplayName("[조회] 성공 - 전화번호(raw) 기반 유저 조회 (hash 변환 후 조회)")
    void loadByPhoneSuccess() {
        // given
        String phoneRaw = "010-1234-5678";
        String hash = "hash-value";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash(hash)
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 1L);

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(hash);
        given(customerRepository.findByContactHash(hash)).willReturn(Optional.of(customer));

        // when
        Customer result = customerService.loadByPhone(phoneRaw);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getGrade()).isEqualTo(Grade.GENERAL);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(hash);
    }

    @Test
    @DisplayName("[조회] 실패 - 고객 없음 (hash 변환 후 조회)")
    void loadByPhoneFailNotFound() {
        // given
        String phoneRaw = "010-9999-9999";
        String hash = "hash-not-found";

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(hash);
        given(customerRepository.findByContactHash(hash)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.loadByPhone(phoneRaw))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(hash);
    }

    @Test
    @DisplayName("[조회] 실패 - phoneRaw null이면 CUSTOMER_NOT_FOUND")
    void loadByPhoneFailNullPhoneRaw() {
        // given
        String phoneRaw = null;

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(null);
        given(customerRepository.findByContactHash(null)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.loadByPhone(phoneRaw))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(null);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 변경(암호화 저장 + 마스킹 응답)")
    void changeEmailEncSuccess() {
        // given
        Long customerId = 1L;
        String plainEmail = "example@example.com";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("old-email-enc")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(plainEmail)).willReturn("new-email-enc");

        ChangeEmailRequest request = new ChangeEmailRequest(plainEmail);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.maskedEmail()).isEqualTo("e***@example.com");

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(plainEmail);

        assertThat(ReflectionTestUtils.getField(customer, "emailEnc")).isEqualTo("new-email-enc");
    }

    @Test
    @DisplayName("[변경] 실패 - 고객 없음 (이메일 변경)")
    void changeEmailEncFailNotFound() {
        // given
        Long customerId = 999L;
        ChangeEmailRequest request = new ChangeEmailRequest("example@example.com");

        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.changeEmailEnc(customerId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(aesUtil);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 마스킹: local 1글자")
    void changeEmailEncMaskLocalLength1() {
        // given
        Long customerId = 1L;
        String email = "a@domain.com";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("old")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(email)).willReturn("enc");

        ChangeEmailRequest request = new ChangeEmailRequest(email);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response.maskedEmail()).isEqualTo("a***@domain.com");

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(email);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 마스킹: local 비어있음")
    void changeEmailEncMaskLocalEmpty() {
        // given
        Long customerId = 1L;
        String email = "@domain.com";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("old")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(email)).willReturn("enc");

        ChangeEmailRequest request = new ChangeEmailRequest(email);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response.maskedEmail()).isEqualTo("***@domain.com");

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(email);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 마스킹: @ 없음이면 null")
    void changeEmailEncMaskNoAtReturnsNull() {
        // given
        Long customerId = 1L;
        String email = "not-an-email";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("old")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(email)).willReturn("enc");

        ChangeEmailRequest request = new ChangeEmailRequest(email);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response.maskedEmail()).isNull();

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(email);
    }

    @Test
    @DisplayName("[변경] 성공 - 고객 등급 변경")
    void changeUserGradeSuccess() {
        // given
        Long customerId = 1L;

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.VIP)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

        ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

        // when
        ChangeGradeResponse response = customerService.changeUserGrade(customerId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.grade()).isEqualTo(Grade.GENERAL);
        assertThat(customer.getGrade()).isEqualTo(Grade.GENERAL);

        verify(customerRepository).findById(customerId);
    }

    @Test
    @DisplayName("[변경] 실패 - 고객 등급 변경 실패(고객 없음)")
    void changeUserGradeFailNotFound() {
        // given
        Long customerId = 999L;
        ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.changeUserGrade(customerId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(aesUtil);
    }
}
