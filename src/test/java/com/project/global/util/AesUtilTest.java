package com.project.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.project.global.exception.core.InvalidStateException;
import com.project.global.exception.core.OperationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AesUtilTest {

  private static final String TEST_SECRET_KEY = "12345678901234567890123456789012";
  private AesUtil aesUtil;

  @BeforeEach
  void setUp() {
    aesUtil = new AesUtil();
    ReflectionTestUtils.setField(aesUtil, "secretKey", TEST_SECRET_KEY);
    aesUtil.init();
  }

  @Test
  @DisplayName("암호화와 복호화가 정상적으로 동작한다")
  void encryptAndDecrypt() {
    // given
    String originalText = "010-1234-5678";

    // when
    String encrypted = aesUtil.encrypt(originalText);
    String decrypted = aesUtil.decrypt(encrypted);

    // then
    assertThat(encrypted).isNotEqualTo(originalText);
    assertThat(decrypted).isEqualTo(originalText);
  }

  @Test
  @DisplayName("null 값을 암호화/복호화하면 null을 반환한다")
  void encryptDecryptNull() {
    // when & then
    assertThat(aesUtil.encrypt(null)).isNull();
    assertThat(aesUtil.decrypt(null)).isNull();
  }

  @Test
  @DisplayName("잘못된 형식의 암호문을 복호화하면 예외가 발생한다")
  void decryptInvalidText() {
    // given
    String invalidText = "NotEncryptedText";

    // when & then
    assertThatThrownBy(() -> aesUtil.decrypt(invalidText))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  @DisplayName("키 길이가 32바이트가 아니면 init() 시 예외가 발생한다")
  void initInvalidKeyLength() {
    // given
    AesUtil invalidAesUtil = new AesUtil();
    ReflectionTestUtils.setField(invalidAesUtil, "secretKey", "shortKey");

    // when & then
    assertThatThrownBy(invalidAesUtil::init).isInstanceOf(InvalidStateException.class);
  }

  @Test
  @DisplayName("복호화 중 잘못된 키나 데이터로 인해 예외 발생 시 OperationFailedException을 던진다")
  void decryptFailThrowsException() {
    // given
    String invalidText = "NotBase64!!";

    // when & then
    assertThatThrownBy(() -> aesUtil.decrypt(invalidText))
        .isInstanceOf(OperationFailedException.class);
  }
}
