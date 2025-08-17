package com.daylily.domain.auth.handler;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.service.UserService;
import com.daylily.domain.github.repository.GitHubAppRepository;
import com.daylily.global.config.GitHubConfig;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import com.daylily.global.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    // 인가 확인용 의존성
    private final GitHubConfig.GitHubClients gh;
    private final GitHubAppRepository gitHubAppRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1) 사용자 저장 (id/login/avatar)
        User user = userService.processOAuth2User(oAuth2User);


        // 3) 설치 토큰 기반 공동작업자 확인 (설치에 포함된 모든 리포에 대해 검사)
        var app = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("GitHub App 미구성"));

        if (app.getInstallationId() == null) {
            log.error("[OAuth2Success] installationId 미설정");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"installation_id_missing\"}");
            return;
        }

        // 설치에 포함된 모든 리포에 대해 공동작업자 여부를 검사한다.
        boolean allowed = false;
        try {
            // 1) App JWT로 앱 컨텍스트에 접근
            String appJwt = gh.issueAppJwt(app.getAppId(), app.getPem());
            GitHub ghAsApp = gh.asApp(appJwt);

            // 2) 설치 권한 클라이언트 생성 (App JWT + installationId 사용)
            GitHub ghAsInstallation = gh.asInstallation(appJwt, app.getInstallationId());
            log.debug("[OAuth2Success] Using installation client for installationId={}", app.getInstallationId());

            // 3) 설치 토큰 발급 (App 자격으로 한 번만)
            var instAsApp = ghAsApp.getApp().getInstallationById(app.getInstallationId());
            String instToken = instAsApp.createToken().create().getToken();

            // 4) 설치 토큰으로 설치 리포 목록 조회 (REST: GET /installation/repositories)
            HttpRequest listReq = HttpRequest.newBuilder(URI.create("https://api.github.com/installation/repositories"))
                    .GET()
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer " + instToken)
                    .build();

            HttpResponse<String> listResp = HttpClient.newHttpClient().send(listReq, HttpResponse.BodyHandlers.ofString());
            if (listResp.statusCode() != 200) {
                throw new RuntimeException("list repos failed: HTTP " + listResp.statusCode());
            }

            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(listResp.body());
            JsonNode repos = root.path("repositories");
            for (JsonNode n : repos) {
                String ownerName = n.path("owner").path("login").asText(null);
                String repoName  = n.path("name").asText(null);
                if (ownerName == null || repoName == null) continue;
                if (gh.hasReadAccess(appJwt, ownerName, repoName, user.getGithubUsername())) {
                    allowed = true;
                    break;
                }
            }
        } catch (Exception e) {
            log.error("[OAuth2Success] 설치 리포 조회/권한 확인 실패", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"authz_check_failed\"}");
            return;
        }

        if (!allowed) {
            log.info("[OAuth2Success] not collaborator on any installed repo. user={}", user.getGithubUsername());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"not_a_collaborator\"}");
            return;
        }

        // 4) JWT 발급 → 헤더 반환
        String accessToken = jwtProvider.createAccessToken(
                Long.valueOf(user.getGithubId()), user.getGithubUsername());

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("X-Username", user.getGithubUsername());
        response.setHeader("Access-Control-Expose-Headers", "Authorization,X-Username");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        String body = new com.fasterxml.jackson.databind.ObjectMapper()
                .createObjectNode()
                .put("ok", true)
                .put("username", user.getGithubUsername())
                .put("github_profile_url", user.getGithubProfileUrl())
                .toString();
        response.getWriter().write(body);
    }
}