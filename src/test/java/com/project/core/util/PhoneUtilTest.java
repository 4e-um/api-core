package com.project.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PhoneUtilTest {

    // 010으로 시작하고 중간 4자리, 끝 4자리 숫자인지 확인하는 정규식
    private static final Pattern PHONE_PATTERN = Pattern.compile("^010-\\d{4}-\\d{4}$");

    @Test
    @DisplayName("랜덤 전화번호 생성 시 010-XXXX-XXXX 형식을 준수한다")
    void generateRandomPhoneNumber() {
        // when
        String phoneNumber = PhoneUtil.generateRandomPhoneNumber();

        // then
        assertThat(phoneNumber).isNotNull();
        assertThat(PHONE_PATTERN.matcher(phoneNumber).matches()).isTrue();
    }

    @Test
    @DisplayName("생성된 번호는 매번 다른 값을 가질 확률이 높다 (100번 시도)")
    void generateRandomPhoneNumberUnique() {
        // given
        final int Attempts = 100;
        Set<String> generatedNumbers = new java.util.HashSet<>();

        // when
        for (int i = 0; i < Attempts; i++) {
            generatedNumbers.add(PhoneUtil.generateRandomPhoneNumber());
        }

        // then
        // 100번 생성했을 때 모든 번호가 고유할 것으로 기대
        assertThat(generatedNumbers).hasSize(Attempts);
    }
}
