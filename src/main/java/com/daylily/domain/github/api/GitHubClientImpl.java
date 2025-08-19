package com.daylily.domain.github.api;

import com.daylily.domain.github.entity.GitHubApp;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.global.config.GithubJwtSigner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubClientImpl implements GitHubClient {

    private final GithubJwtSigner signer;
    private final ObjectMapper objectMapper;

    private Map<String, Object> decodeJwtPayload(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("invalid JWT format");
        }

        byte[] json = Base64.getUrlDecoder().decode(parts[1]);
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    private boolean isUrlSafe(String s) {
        return s != null && s.matches("[A-Za-z0-9_-]+");
    }

    private Long extractLongValue(Object value) {
        switch (value) {
            case Number number -> {
                return number.longValue();
            }
            case String str -> {
                try {
                    return Long.valueOf(str);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            case null, default -> {
                return null;
            }
        }
    }

    // AppId+PEM 으로 App JWT 발급
    private String issueAppJwt(long appId, String pem) {
        String jwt = signer.sign(appId, pem);
        try {
            String[] parts = jwt.split("\\.");
            boolean hOk = parts.length == 3 && isUrlSafe(parts[0]);
            boolean pOk = parts.length == 3 && isUrlSafe(parts[1]);
            boolean sOk = parts.length == 3 && isUrlSafe(parts[2]);

            Map<String, Object> payload = decodeJwtPayload(jwt);
            Long issN = extractLongValue(payload.get("iss"));
            Long iatN = extractLongValue(payload.get("iat"));
            Long expN = extractLongValue(payload.get("exp"));
            long now = Instant.now().getEpochSecond();

            log.debug("[GitHubClients] App JWT minted: iss={} (expected {}), iat={}, exp={}, now={}, ttl={}s, urlSafe(h/p/s)=[{}/{}/{}]",
                    issN, appId, iatN, expN, now, (expN != null && iatN != null ? (expN - iatN) : -1), hOk, pOk, sOk);

            if (issN == null || issN != appId) {
                log.warn("[GitHubClients] WARNING: JWT iss does not match appId (iss={}, appId={})", issN, appId);
            }
            if (iatN != null && expN != null && (expN - iatN) > 600) {
                log.warn("[GitHubClients] WARNING: JWT exp-iat exceeds 10 minutes: {}s", (expN - iatN));
            }
            if (iatN != null && (now + 30) < iatN) {
                log.warn("[GitHubClients] WARNING: JWT iat is in the future. clock skew? now={}, iat={}", now, iatN);
            }
            if (expN != null && now >= expN) {
                log.warn("[GitHubClients] WARNING: JWT expired. now={}, exp={}", now, expN);
            }
        } catch (Exception e) {
            log.warn("[GitHubClients] Failed to decode/log JWT payload: {}", e.toString());
        }
        return jwt;
    }

    @Override
    public GitHub withOAuth(String accessToken) {
        try {
            return new GitHubBuilder().withOAuthToken(accessToken).build();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub API 연결 실패: " + e.getMessage());
        }
    }

    @Override
    public GitHub withApp(GitHubApp app) {
        if (app.getAppId() == null || app.getPem() == null) {
            throw new GitHubException(GitHubErrorCode.APP_NOT_FOUND, "GitHub API 연결에 필요한 정보 누락");
        }

        String jwt = issueAppJwt(app.getAppId(), app.getPem());
        try {
            return new GitHubBuilder().withJwtToken(jwt).build();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub API 연결 실패: " + e.getMessage());
        }
    }

    @Override
    public GitHub withAppInstallation(GitHubApp app) {
        if (app.getAppId() == null || app.getPem() == null || app.getInstallationId() == null) {
            throw new GitHubException(GitHubErrorCode.APP_NOT_FOUND, "GitHub API 연결에 필요한 정보 누락");
        }

        GitHub ghWithApp = this.withApp(app);

        try {
            GHAppInstallation installation = ghWithApp.getApp().getInstallationById(app.getInstallationId());
            String installationToken = installation.createToken().create().getToken();
            return new GitHubBuilder().withAppInstallationToken(installationToken).build();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub API 연결 실패: " + e.getMessage());
        }
    }
}
