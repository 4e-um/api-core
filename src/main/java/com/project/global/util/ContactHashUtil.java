package com.project.global.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContactHashUtil {

    @Value("${ureca.hash-key}") // ✅ base64 문자열이어야 함 (Python이 base64decode 하니까)
    private String hashKey;

    private SecretKeySpec keySpec;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            // ✅ Python: base64.b64decode(HASH_KEY) 와 동일
            byte[] keyBytes = Base64.getDecoder().decode(hashKey);
            this.keySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (IllegalArgumentException e) {
            // hashKey가 base64가 아니면 여기로 옴
            throw new IllegalStateException("ureca.hash-key must be Base64 encoded", e);
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
            throw new IllegalStateException(e);
        }
    }
}