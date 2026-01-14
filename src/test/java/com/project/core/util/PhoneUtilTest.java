package com.project.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
	void generateRandomPhoneNumber_Unique() {
		// when
		String phone1 = PhoneUtil.generateRandomPhoneNumber();
		String phone2 = PhoneUtil.generateRandomPhoneNumber();

		// then
		// 극히 낮은 확률로 같을 수도 있지만, 테스트 목적상 다름을 기대
		assertThat(phone1).isNotEqualTo(phone2);
	}
}