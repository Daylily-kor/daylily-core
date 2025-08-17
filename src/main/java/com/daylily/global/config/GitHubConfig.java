package com.daylily.global.config;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.daylily.global.config.GithubJwtSigner;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 * 용도별(Anonymous, User token, App JWT, Installation token)
 * 간편하게 클라이언트를 만들 수 있는 팩토리를 제공.
 * 필요 시 AppId+PEM으로 App JWT를 서명해 발급하는 편의 메서드도 함께 제공한다.
 *  - asAnonymous()                      : 비인증(레이트리밋 낮음)
 *  - asUser(userToken)                  : GitHub App User Access Token으로 사용자로 행동
 *  - asApp(appJwt | appId+pem)          : App JWT로 앱으로 행동(설치 조회/토큰 발급 용)
 *  - asInstallation(appJwt | appId+pem, installationId | owner/repo)
 *                                        : 설치 토큰을 만들어 설치 권한으로 행동
 *  - hasReadAccess(...)                 : 설치 토큰으로 특정 사용자의 collaborator/read 권한 확인
 */
@Configuration
public class GitHubConfig {

    @Bean
    public GitHubClients gitHubClients(GithubJwtSigner signer) {
        return new GitHubClients(signer);
    }

    @Slf4j
    public static class GitHubClients {
        private final GithubJwtSigner signer;
        public GitHubClients(GithubJwtSigner signer) { this.signer = signer; }

        private static final ObjectMapper MAPPER = new ObjectMapper();

        private static String redact(String token) {
            if (token == null) return "null";
            int n = token.length();
            return n <= 16 ? "***" : token.substring(0, 12) + "..." + token.substring(n - 6);
        }

        private static Map<String, Object> decodeJwtPayload(String jwt) throws Exception {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("invalid JWT format");
            byte[] json = Base64.getUrlDecoder().decode(parts[1]);
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        }

        private static boolean isUrlSafe(String s) {
            return s != null && s.matches("[A-Za-z0-9_-]+");
        }

        // AppId+PEM 으로 App JWT 발급
        public String issueAppJwt(long appId, String pem) {
            String jwt = signer.sign(appId, pem);
            try {
                String[] parts = jwt.split("\\.");
                boolean hOk = parts.length == 3 && isUrlSafe(parts[0]);
                boolean pOk = parts.length == 3 && isUrlSafe(parts[1]);
                boolean sOk = parts.length == 3 && isUrlSafe(parts[2]);

                Map<String, Object> payload = decodeJwtPayload(jwt);
                Number issN = (Number) payload.get("iss");
                Number iatN = (Number) payload.get("iat");
                Number expN = (Number) payload.get("exp");
                long now = Instant.now().getEpochSecond();

                log.debug("[GitHubClients] App JWT minted: iss={} (expected {}), iat={}, exp={}, now={}, ttl={}s, urlSafe(h/p/s)=[{}/{}/{}]",
                        issN, appId, iatN, expN, now, (expN != null && iatN != null ? (expN.longValue() - iatN.longValue()) : -1), hOk, pOk, sOk);

                if (issN == null || issN.longValue() != appId) {
                    log.warn("[GitHubClients] WARNING: JWT iss does not match appId (iss={}, appId={})", issN, appId);
                }
                if (iatN != null && expN != null && (expN.longValue() - iatN.longValue()) > 600) {
                    log.warn("[GitHubClients] WARNING: JWT exp-iat exceeds 10 minutes: {}s", (expN.longValue() - iatN.longValue()));
                }
                if (iatN != null && (now + 30) < iatN.longValue()) {
                    log.warn("[GitHubClients] WARNING: JWT iat is in the future. clock skew? now={}, iat={}", now, iatN);
                }
                if (expN != null && now >= expN.longValue()) {
                    log.warn("[GitHubClients] WARNING: JWT expired. now={}, exp={}", now, expN);
                }
            } catch (Exception e) {
                log.warn("[GitHubClients] Failed to decode/log JWT payload: {}", e.toString());
            }
            return jwt;
        }

        // AppId+PEM 으로 앱 클라이언트
        public GitHub asApp(long appId, String pem) { return asApp(issueAppJwt(appId, pem)); }

        // AppId+PEM+installationId 로 설치 클라이언트
        public GitHub asInstallation(long appId, String pem, long installationId) {
            return asInstallation(issueAppJwt(appId, pem), installationId);
        }

        // AppId+PEM+owner/repo 로 설치 클라이언트
        public GitHub asInstallation(long appId, String pem, String owner, String repo) {
            return asInstallation(issueAppJwt(appId, pem), owner, repo);
        }

        // AppId+PEM 으로 read 권한 확인
        public boolean hasReadAccess(long appId, String pem, String owner, String repo, String githubLogin) {
            return hasReadAccess(issueAppJwt(appId, pem), owner, repo, githubLogin);
        }

        // 사용자 토큰(UAT)로 동작하는 클라이언트
        public GitHub asUser(String userAccessToken) {
            try {
                return new GitHubBuilder().withOAuthToken(userAccessToken).build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build user GitHub client", e);
            }
        }

        // 앱 JWT로 동작하는 클라이언트 (설치 조회/토큰 발급에 사용)
        public GitHub asApp(String appJwt) {
            try {
                log.debug("[GitHubClients] Build GitHub (APP) withJwtToken jwt={}", redact(appJwt));
                return new GitHubBuilder().withJwtToken(appJwt).build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build app GitHub client", e);
            }
        }

        // 설치 ID로 설치 토큰을 발급
        public GitHub asInstallation(String appJwt, long installationId) {
            try {
                log.debug("[GitHubClients] Build GitHub (INSTALLATION by id) appJwt={}, installationId={}", redact(appJwt), installationId);
                GitHub ghAsApp = asApp(appJwt);
                GHAppInstallation inst = ghAsApp.getApp().getInstallationById(installationId);
                String instToken = inst.createToken().create().getToken();
                log.debug("[GitHubClients] Minted installation token (by id) len={}", (instToken != null ? instToken.length() : -1));
                return new GitHubBuilder().withAppInstallationToken(instToken).build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build installation GitHub client (by id)", e);
            }
        }

        // owner/repo로 설치를 해석해 설치 토큰을 발급하는 클라이언트
        public GitHub asInstallation(String appJwt, String owner, String repo) {
            try {
                log.debug("[GitHubClients] Build GitHub (INSTALLATION by repo) appJwt={}, {}/{}", redact(appJwt), owner, repo);
                GitHub ghAsApp = asApp(appJwt);
                GHAppInstallation inst = ghAsApp.getApp().getInstallationByRepository(owner, repo);
                String instToken = inst.createToken().create().getToken();
                log.debug("[GitHubClients] Minted installation token (by repo) len={}", (instToken != null ? instToken.length() : -1));
                return new GitHubBuilder().withAppInstallationToken(instToken).build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build installation GitHub client (by repo)", e);
            }
        }

        // 설치 토큰만 문자열로 발급 (owner/repo 기준)
        private String installationTokenByRepo(String appJwt, String owner, String repo) {
            try {
                log.debug("[GitHubClients] Mint installation token for {}/{} with appJwt={}", owner, repo, redact(appJwt));
                GitHub ghAsApp = asApp(appJwt);
                GHAppInstallation inst = ghAsApp.getApp().getInstallationByRepository(owner, repo);
                return inst.createToken().create().getToken();
            } catch (Exception e) {
                throw new RuntimeException("Failed to mint installation token (by repo)", e);
            }
        }

        // 특정 사용자가 권한이 있는지 검사
        public boolean hasReadAccess(String appJwt, String owner, String repo, String githubLogin) {
            try {
                // 1. Mint installation token for the repo
                String instToken = installationTokenByRepo(appJwt, owner, repo);

                log.debug("[GitHubClients] Check collaborator: {}/{} user={} (install token minted)", owner, repo, githubLogin);

                // 2. Call collaborators check endpoint
                String url = "https://api.github.com/repos/" + owner + "/" + repo + "/collaborators/" + githubLogin;
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .GET()
                        .header("Accept", "application/vnd.github+json")
                        .header("Authorization", "Bearer " + instToken)
                        .build();

                HttpResponse<Void> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.discarding());
                int sc = resp.statusCode();
                log.debug("[GitHubClients] Collaborator check HTTP status: {}", sc);
                if (sc == 204) return true;
                if (sc == 404) return false;

                throw new RuntimeException("GitHub collaborator check failed: HTTP " + sc);
            } catch (Exception e) {
                throw new RuntimeException("공동작업자 검증에 실패했습니다.", e);
            }
        }
    }
}
