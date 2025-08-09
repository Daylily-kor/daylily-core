package com.daylily.global.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Converter
public class AesGcmStringConverter implements AttributeConverter<String, String> {

    private static final String ALG = "AES/GCM/NoPadding";
    private static final int TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmStringConverter() {
        // 환경 변수에서 비밀 키를 가져옵니다.
        // Git Bash에서 `openssl rand -base64 32` 실행 후 나온 키 값을 사용
        String key = System.getenv("DAYLILY_SECRET_KEY");
        if (key == null) {
            throw new IllegalStateException("Environment variable DAYLILY_SECRET_KEY is not set");
        }
        this.key = new SecretKeySpec(Base64.getDecoder().decode(key.getBytes(StandardCharsets.UTF_8)), "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            byte[] all = Base64.getDecoder().decode(dbData);
            byte[] iv = Arrays.copyOfRange(all, 0, IV_BYTES);
            byte[] cipherText = Arrays.copyOfRange(all, IV_BYTES, all.length);

            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
