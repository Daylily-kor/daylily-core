package com.daylily.global.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;


@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final long ACCESS_TOKEN_VALIDITY_SECONDS = 1000 * 60 * 60 * 3;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY_SECONDS);

        return Jwts.builder()
                .setSubject("AccessToken")
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createAppJwt(long appId, String pem) {
        RSAPrivateKey key = readPrivateKeyFromPem(pem);

        Instant now = Instant.now();
        Date iat = Date.from(now.minusSeconds(60));     // 클럭 드리프트 버퍼
        Date exp = Date.from(now.plusSeconds(9 * 60));  // 10분 미만

        return JWT.create()
                .withIssuer(String.valueOf(appId))
                .withIssuedAt(iat)
                .withExpiresAt(exp)
                .sign(Algorithm.RSA256(null, key));
    }

    private RSAPrivateKey readPrivateKeyFromPem(String pem) {
        try {
            Security.addProvider(new BouncyCastleProvider());

            // DB에는 본문만 저장되어 있으므로 헤더가 없으면 PKCS#8/PKCS#1 두 가지로 감싸서 시도
            String normalized = pem.strip();

            if (!normalized.contains("BEGIN")) {
                String body = normalized.replaceAll("\\s+", "");
                // 64자 줄바꿈을 적당히 넣어주면 파서가 더 잘 읽습니다
                String lines = body.replaceAll("(.{64})", "$1\n");

                // 1) PKCS#8
                String pkcs8 = "-----BEGIN PRIVATE KEY-----\n" + lines + "\n-----END PRIVATE KEY-----\n";
                try { return parsePem(pkcs8); } catch (Exception ignore) {}

                // 2) PKCS#1 (RSA)
                String pkcs1 = "-----BEGIN RSA PRIVATE KEY-----\n" + lines + "\n-----END RSA PRIVATE KEY-----\n";
                return parsePem(pkcs1);
            }

            return parsePem(normalized);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid GitHub App PEM", e);
        }
    }

    private RSAPrivateKey parsePem(String pemWithHeaders) throws Exception {
        try (PEMParser parser = new PEMParser(new StringReader(pemWithHeaders))) {
            Object obj = parser.readObject();
            var conv = new JcaPEMKeyConverter().setProvider("BC");

            PrivateKey pk;
            if (obj instanceof PrivateKeyInfo pki) {
                pk = conv.getPrivateKey(pki);             // PKCS#8
            } else if (obj instanceof PEMKeyPair kp) {
                pk = conv.getKeyPair(kp).getPrivate();    // PKCS#1
            } else {
                throw new IllegalStateException("Unsupported PEM type: " + obj);
            }
            return (RSAPrivateKey) pk;
        }
    }


}
