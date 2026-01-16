package com.project.global.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.InvalidStateException;
import com.project.global.exception.core.OperationFailedException;

@Component
public class AesUtil {

    @Value("${ureca.secret-key}")
    private String secretKey;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @PostConstruct
    public void init() {
        // AES-256은 32바이트(256비트) 키가 필수입니다.
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new InvalidStateException(CoreErrorCode.AES_KEY_LENGTH_INCORRECT);
        }
    }

    // 암호화 (Encrypt)
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            // 1. 랜덤 IV(Initialization Vector) 생성 (16바이트)
            byte[] iv = new byte[16];
            SECURE_RANDOM.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 2. 키 생성
            SecretKeySpec keySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            // 3. 암호화 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 4. 암호화 수행
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 5. 결과 합치기 (IV + EncryptedBytes)
            // 복호화할 때 IV가 필요하므로 앞에 붙여서 저장함
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // 6. Base64 인코딩하여 반환
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new OperationFailedException(CoreErrorCode.ENCRYPTION_FAILED);
        }
    }

    // 복호화 (Decrypt)
    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }

        try {
            // 1. Base64 디코딩
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // 2. IV와 암호문 분리
            byte[] iv = new byte[16];
            System.arraycopy(decoded, 0, iv, 0, 16);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] encrypted = new byte[decoded.length - 16];
            System.arraycopy(decoded, 16, encrypted, 0, encrypted.length);

            // 3. 키 생성
            SecretKeySpec keySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            // 4. 복호화 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 5. 복호화 수행 및 문자열 반환
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new OperationFailedException(CoreErrorCode.ENCRYPTION_FAILED);
        }
    }
}
