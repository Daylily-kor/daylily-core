package com.daylily.global.config;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;

@Component
public class GithubJwtSigner {

    /**
     * GitHub App JWT 생성 (RS256).
     * iss = appId, iat = now-30s(시계 오차 보정), exp = iat + 9분(<=10분 권장)
     */
    public String sign(long appId, String pemPrivateKey) {
        return sign(appId, pemPrivateKey, Duration.ofMinutes(9));
    }

    public String sign(long appId, String pemPrivateKey, Duration ttl) {
        try {
            RSAPrivateKey key = loadRsaPrivateKey(pemPrivateKey);

            long now = Instant.now().minusSeconds(30).getEpochSecond(); // clock skew
            long exp = now + Math.min(ttl.getSeconds(), 9 * 60);        // safety cap 9m

            String headerJson  = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
            String payloadJson = "{\"iat\":" + now + ",\"exp\":" + exp + ",\"iss\":\"" + appId + "\"}";

            String header  = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
            String payload = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signingInput = header + "." + payload;

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(key);
            sig.update(signingInput.getBytes(StandardCharsets.UTF_8));
            String signature = base64Url(sig.sign());

            return signingInput + "." + signature;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to sign GitHub App JWT", e);
        }
    }

    // ===== helpers =====

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * PEM 문자열에서 RSA 개인키 로드 (PKCS#1: -----BEGIN RSA PRIVATE KEY----- 전용).
     * Java KeyFactory는 PKCS#1을 직접 읽지 못하므로 PKCS#8으로 래핑하여 로드한다.
     */
    private static RSAPrivateKey loadRsaPrivateKey(String pem) throws GeneralSecurityException {
        // Expect PKCS#1 PEM: -----BEGIN RSA PRIVATE KEY----- ... -----END RSA PRIVATE KEY-----
        String normalized = pem
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] pkcs1Der = Base64.getDecoder().decode(normalized);
        // Wrap PKCS#1 -> PKCS#8 (PrivateKeyInfo with rsaEncryption OID)
        byte[] pkcs8Der = wrapPkcs1ToPkcs8(pkcs1Der);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Der));
    }

    private static byte[] wrapPkcs1ToPkcs8(byte[] pkcs1Der) {
        try {
            // AlgorithmIdentifier for rsaEncryption OID 1.2.840.113549.1.1.1 with NULL param
            final byte[] algId = new byte[] {
                    0x30, 0x0D,
                    0x06, 0x09, 0x2A, (byte)0x86, 0x48, (byte)0x86, (byte)0xF7, 0x0D, 0x01, 0x01, 0x01,
                    0x05, 0x00
            };

            // version INTEGER 0
            ByteArrayOutputStream pki = new ByteArrayOutputStream();
            pki.write(0x02); pki.write(0x01); pki.write(0x00);           // INTEGER 0
            pki.write(algId);                                            // AlgorithmIdentifier
            pki.write(0x04);                                             // OCTET STRING
            writeDerLength(pki, pkcs1Der.length);
            pki.write(pkcs1Der);

            byte[] pkiContent = pki.toByteArray();

            ByteArrayOutputStream seq = new ByteArrayOutputStream();
            seq.write(0x30);                                             // SEQUENCE
            writeDerLength(seq, pkiContent.length);
            seq.write(pkiContent);
            return seq.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to wrap PKCS#1 to PKCS#8", e);
        }
    }

    private static void writeDerLength(ByteArrayOutputStream out, int len) {
        if (len < 0x80) {
            out.write(len);
        } else if (len <= 0xFF) {
            out.write(0x81); out.write(len);
        } else if (len <= 0xFFFF) {
            out.write(0x82); out.write((len >> 8) & 0xFF); out.write(len & 0xFF);
        } else if (len <= 0xFFFFFF) {
            out.write(0x83); out.write((len >> 16) & 0xFF); out.write((len >> 8) & 0xFF); out.write(len & 0xFF);
        } else {
            out.write(0x84);
            out.write((len >> 24) & 0xFF); out.write((len >> 16) & 0xFF);
            out.write((len >> 8) & 0xFF);  out.write(len & 0xFF);
        }
    }
}