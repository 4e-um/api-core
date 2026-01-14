package com.project.global.util;

import com.project.global.exception.core.InvalidStateException;
import com.project.global.exception.core.OperationFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AESUtilTest {

	@Test
	@DisplayName("암호화와 복호화가 정상적으로 동작한다")
	void encryptAndDecrypt() {
		// given
		AESUtil aesUtil = new AESUtil();
		// 테스트용 32바이트 키 주입 (application.yml 값 시뮬레이션)
		ReflectionTestUtils.setField(aesUtil, "secretKey", "12345678901234567890123456789012");

		String originalText = "010-1234-5678";

		// when
		String encrypted = aesUtil.encrypt(originalText);
		String decrypted = aesUtil.decrypt(encrypted);

		// then
		assertThat(encrypted).isNotEqualTo(originalText); // 암호화된 값은 원본과 달라야 함
		assertThat(decrypted).isEqualTo(originalText);    // 복호화된 값은 원본과 같아야 함
	}

	@Test
	@DisplayName("null 값을 암호화/복호화하면 null을 반환한다")
	void encryptDecryptNull() {
		// given
		AESUtil aesUtil = new AESUtil();
		ReflectionTestUtils.setField(aesUtil, "secretKey", "12345678901234567890123456789012");

		// when & then
		assertThat(aesUtil.encrypt(null)).isNull();
		assertThat(aesUtil.decrypt(null)).isNull();
	}

	@Test
	@DisplayName("잘못된 형식의 암호문을 복호화하면 예외가 발생한다")
	void decryptInvalidText() {
		// given
		AESUtil aesUtil = new AESUtil();
		ReflectionTestUtils.setField(aesUtil, "secretKey", "12345678901234567890123456789012");
		String invalidText = "NotEncryptedText";

		// when & then
		assertThatThrownBy(() -> aesUtil.decrypt(invalidText))
						.isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("키 길이가 32바이트가 아니면 init() 시 예외가 발생한다")
	void init_InvalidKeyLength() {
		AESUtil aesUtil = new AESUtil();
		// 32바이트가 아닌 짧은 키 주입
		ReflectionTestUtils.setField(aesUtil, "secretKey", "shortKey");

		assertThatThrownBy(aesUtil::init)
						.isInstanceOf(InvalidStateException.class);
	}

	@Test
	@DisplayName("복호화 중 잘못된 키나 데이터로 인해 예외 발생 시 OperationFailedException을 던진다")
	void decrypt_Fail_ThrowsException() {
		AESUtil aesUtil = new AESUtil();
		ReflectionTestUtils.setField(aesUtil, "secretKey", "12345678901234567890123456789012");
		aesUtil.init();

		// Base64 포맷조차 아닌 엉뚱한 문자열 -> IllegalArgumentException 유발 -> catch에서 잡힘
		String invalidText = "NotBase64!!";

		assertThatThrownBy(() -> aesUtil.decrypt(invalidText))
						.isInstanceOf(OperationFailedException.class);
	}
}