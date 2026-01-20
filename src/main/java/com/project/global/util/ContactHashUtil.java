package com.project.global.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.InvalidStateException;

@Component
public class ContactHashUtil {

    @Value("${ureca.hash-key}") // ✅ base64 문자열이어야 함 (Python이 base64decode 하니까)
    private String hashKey;

    private SecretKeySpec keySpec;

    @PostConstruct
    public void init() {
        try {
            // ✅ Python: base64.b64decode(HASH_KEY) 와 동일
            byte[] keyBytes = Base64.getDecoder().decode(hashKey);
            this.keySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            this.hashKey = null; // 초기화 후 메모리에서 평문 키를 제거하여 보안을 강화합니다.
        } catch (IllegalArgumentException e) {
            // hashKey가 base64가 아니면 여기로 옴
            throw new InvalidStateException(CoreErrorCode.ENCRYPTION_MUST_BE_BASE64);
        }
    }

    public String hmacSha256Base64(String value) {
        if (value == null) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);

            byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // ✅ Python: base64.b64encode(signature).decode('utf-8') 와 동일
            return Base64.getEncoder().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new InvalidStateException(CoreErrorCode.ENCRYPTION_FAILED);
        }
    }
}
