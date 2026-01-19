package com.project.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.InvalidStateException;

class ContactHashUtilTest {

    @Test
    @DisplayName(
            "[성공] hmacSha256Base64 - Python(base64decode key + HMAC-SHA256 + base64encode)와 동일")
    void hmacSha256Base64_success_matchesExpected() throws Exception {
        // given
        ContactHashUtil util = new ContactHashUtil();

        byte[] keyBytes = "my-secret-hmac-key-bytes".getBytes(StandardCharsets.UTF_8);
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        ReflectionTestUtils.setField(util, "hashKey", base64Key);
        util.init(); // ✅ PostConstruct 수동 호출

        String value = "010-1234-5678";

        // expected
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        String expected = Base64.getEncoder().encodeToString(raw);

        // when
        String actual = util.hmacSha256Base64(value);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("[성공] value가 null이면 null 반환")
    void hmacSha256Base64_valueNull_returnsNull() {
        // given
        ContactHashUtil util = new ContactHashUtil();

        // init() 호출 없어도 null이면 바로 return이라 상관없지만,
        // 테스트 일관성을 위해 넣어도 됨(아래처럼 정상 base64 넣고 init 호출)
        String base64Key = Base64.getEncoder().encodeToString("k".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(util, "hashKey", base64Key);
        util.init();

        // when
        String actual = util.hmacSha256Base64(null);

        // then
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("[실패] hashKey가 base64가 아니면 InvalidStateException(ENCRYPTION_MUST_BE_BASE64)")
    void hmacSha256Base64_hashKeyNotBase64_throws() {
        // given
        ContactHashUtil util = new ContactHashUtil();
        ReflectionTestUtils.setField(util, "hashKey", "NOT_BASE64_###"); // base64 아님

        // when & then (init에서 예외)
        assertThatThrownBy(util::init)
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(CoreErrorCode.ENCRYPTION_MUST_BE_BASE64.getMessage());
    }

    @Test
    @DisplayName("[성공] 같은 입력/같은 키면 결과는 항상 동일(결정적)")
    void hmacSha256Base64_sameInputSameKey_sameOutput() {
        // given
        ContactHashUtil util = new ContactHashUtil();
        String base64Key =
                Base64.getEncoder().encodeToString("fixed-key".getBytes(StandardCharsets.UTF_8));

        ReflectionTestUtils.setField(util, "hashKey", base64Key);
        util.init(); // ✅ PostConstruct 수동 호출

        String value = "01012345678";

        // when
        String hashA = util.hmacSha256Base64(value);
        String hashB = util.hmacSha256Base64(value);

        // then
        assertThat(hashA).isEqualTo(hashB);
    }
}
