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

class ContactHashUtilTest {

    @Test
    @DisplayName("[성공] hmacSha256Base64 - Python(base64decode key + HMAC-SHA256 + base64encode)와 동일")
    void hmacSha256Base64_success_matchesExpected() throws Exception {
        // given
        ContactHashUtil util = new ContactHashUtil();

        // keyBytes(임의) -> base64 문자열로 만들어 주입
        byte[] keyBytes = "my-secret-hmac-key-bytes".getBytes(StandardCharsets.UTF_8);
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        ReflectionTestUtils.setField(util, "hashKey", base64Key);

        String value = "010-1234-5678";

        // expected (테스트에서 직접 동일 알고리즘으로 계산)
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
        ReflectionTestUtils.setField(util, "hashKey", Base64.getEncoder().encodeToString("k".getBytes(StandardCharsets.UTF_8)));

        // when
        String actual = util.hmacSha256Base64(null);

        // then
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("[실패] hashKey가 base64가 아니면 IllegalStateException 발생 + 메시지 검증")
    void hmacSha256Base64_hashKeyNotBase64_throws() {
        // given
        ContactHashUtil util = new ContactHashUtil();
        ReflectionTestUtils.setField(util, "hashKey", "NOT_BASE64_###"); // base64 아님

        // when & then
        assertThatThrownBy(() -> util.hmacSha256Base64("010-1234-5678"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ureca.hash-key must be Base64 encoded");
    }

    @Test
    @DisplayName("[성공] 같은 입력/같은 키면 결과는 항상 동일(결정적)")
    void hmacSha256Base64_sameInputSameKey_sameOutput() {
        // given
        ContactHashUtil util = new ContactHashUtil();
        String base64Key = Base64.getEncoder().encodeToString("fixed-key".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(util, "hashKey", base64Key);

        String value = "01012345678";

        // when
        String a = util.hmacSha256Base64(value);
        String b = util.hmacSha256Base64(value);

        // then
        assertThat(a).isEqualTo(b);
    }
}
