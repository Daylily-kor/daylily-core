package com.daylily.domain.github.util;

import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;


@Component
@RequiredArgsConstructor
public class WebhookSignatureVerifier {

    /**
     * GitHub Webhook 요청의 유효성을 검증하기 위한 요청 정보입니다.
     * @param targetId 웹훅을 보낸 GitHub App의 ID
     * @param signature 웹훅 요청의 헤더에 포함된 X-Hub-Signature-256 값
     * @param payload 웹훅 요청의 본문(payload) 내용
     */
    public record VerificationRequest(
            Long targetId,
            String signature,
            String payload
    ) {
        public VerificationRequest(ContentCachingRequestWrapper request) throws UnsupportedEncodingException {
            this(
                    Long.parseLong(request.getHeader("X-GitHub-Hook-Installation-Target-ID")),
                    request.getHeader("X-Hub-Signature-256"),
                    new String(request.getContentAsByteArray(), request.getCharacterEncoding())
            );
        }
    }

    private static final String PREFIX = "sha256=";

    private final GitHubAppRepository repository;

    /**
     * signatureHeader와 DB에 저장된 webhook secret + payload의 HMAC-SHA256 해시값을 비교하여
     * GitHub Webhook 요청의 유효성을 검증합니다.
     */
    public void verify(VerificationRequest request) {
        String signature = request.signature();

        if (signature == null || !signature.startsWith(PREFIX)) {
            throw new GitHubException(GitHubErrorCode.INVALID_HEADER_SECRET);
        }

        String secret = repository.findByAppId(request.targetId())
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.SECRET_NOT_FOUND))
                .getWebhookSecret();

        String expectedSignature = PREFIX + hmacHex(secret, request.payload());
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new GitHubException(GitHubErrorCode.INVALID_HEADER_SECRET);
        }
    }

    private String hmacHex(String secret, String body) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().withLowerCase().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_SECRET_VERIFICATION_FAILED);
        }
    }
}
