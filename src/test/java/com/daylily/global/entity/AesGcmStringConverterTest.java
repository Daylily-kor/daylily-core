package com.daylily.global.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesGcmStringConverterTest {

    private static AesGcmStringConverter converter;

    @BeforeAll
    static void setup() {
        String key = System.getenv("DAYLILY_SECRET_KEY");
        assertNotNull(key, "Environment variable DAYLILY_SECRET_KEY is not set");
        converter = new AesGcmStringConverter();
    }

    @Test
    void testRoundTripEncryption() {
        String raw = "my-secret-value";
        String encrypted = converter.convertToDatabaseColumn(raw);
        assertNotNull(encrypted);
        assertNotEquals(raw, encrypted, "Encrypted text should differ from plain text");

        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(raw, decrypted, "Decrypted text should equal plain text");
    }

    @Test
    void testDifferentIvProducesDifferentCiphertext() {
        String raw = "repeatable-secret";
        String c1 = converter.convertToDatabaseColumn(raw);
        String c2 = converter.convertToDatabaseColumn(raw);
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotEquals(c1, c2, "Different IVs should produce different ciphertexts");
    }

    @Test
    void testNullHandling() {
        assertNull(converter.convertToDatabaseColumn(null), "Null input should return null");
        assertNull(converter.convertToEntityAttribute(null), "Null input should return null");
    }

    @Test
    void invalidEncryptedStringThrowsException() {
        String invalidEncrypted = "invalid-encrypted-string";
        assertThrows(RuntimeException.class, () -> {
            converter.convertToEntityAttribute(invalidEncrypted);
        }, "Invalid encrypted string should throw an exception");
    }
}
