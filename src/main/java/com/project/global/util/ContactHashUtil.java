package com.project.global.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContactHashUtil {

  @Value("${ureca.hash-key}") // AES 키랑 분리 추천
  private String hashKey;

  public String hmacSha256Hex(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec keySpec = new SecretKeySpec(hashKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(keySpec);
      byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder(raw.length * 2);
      for (byte b : raw) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
