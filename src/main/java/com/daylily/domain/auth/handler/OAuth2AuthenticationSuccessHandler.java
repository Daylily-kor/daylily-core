package com.daylily.domain.auth.handler;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.service.UserService;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import com.daylily.domain.github.service.GitHubAppAuthService;
import com.daylily.global.config.GitHubClient;
import com.daylily.global.jwt.JwtProvider;
import com.daylily.global.response.ErrorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GitHub;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final GitHubAppAuthService gitHubAppAuthService;

    private final GitHubAppRepository gitHubAppRepository;

    private final RestClient gitHubRestClient;
    private final GitHubClient githubClient;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        GitHubAppAuthService.AuthResult result = gitHubAppAuthService.authenticateGitHubUser(oAuth2User, response);

        if (!result.success()) {
            log.error("[OAuth2AuthenticationSuccessHandler] 인증 실패: {}", result.errorMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(GitHubErrorCode.UNAUTHORIZED_REPOSITORY, result.errorMessage()));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("http://localhost:3000/auth/callback?ok=true");
    }

    //    @Override
    public void __onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1) 사용자 저장 (id/login/avatar)
        User user = userService.processOAuth2User(oAuth2User);

        // 3) 설치 토큰 기반 공동작업자 확인 (설치에 포함된 모든 리포에 대해 검사)
        var app = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        if (app.getInstallationId() == null) {
            log.error("[OAuth2Success] installationId 미설정");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(GitHubErrorCode.INSTALLATION_NOT_FOUND));
            return;
        }

        // 설치에 포함된 모든 리포에 대해 공동작업자 여부를 검사한다.
        boolean allowed = false;
        try {
            // 1) App JWT로 앱 컨텍스트에 접근
            String appJwt = githubClient.issueAppJwt(app.getAppId(), app.getPem());
            GitHub ghAsApp = githubClient.asApp(appJwt);

            // 2) 설치 권한 클라이언트 생성 (App JWT + installationId 사용)
            GitHub ghAsInstallation = githubClient.asInstallation(appJwt, app.getInstallationId());
            log.debug("[OAuth2Success] Using installation client for installationId={}", app.getInstallationId());

            // 3) 설치 토큰 발급 (App 자격으로 한 번만)
            var instAsApp = ghAsApp.getApp().getInstallationById(app.getInstallationId());
            String instToken = instAsApp.createToken().create().getToken();

            // 4) 설치 토큰으로 설치 리포 목록 조회 (REST: GET /installation/repositories)
            JsonNode root = gitHubRestClient
                    .get()
                    .uri("/installation/repositories")
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer " + instToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new GitHubException(GitHubErrorCode.GITHUB_LIST_REPOS_ERROR);
                    })
                    .body(JsonNode.class);

            assert root != null;
            JsonNode repos = root.path("repositories");
            for (JsonNode n : repos) {
                String ownerName = n.path("owner").path("login").asText(null);
                String repoName  = n.path("name").asText(null);
                if (ownerName == null || repoName == null) continue;
                if (githubClient.hasReadAccess(appJwt, ownerName, repoName, user.getGithubUsername())) {
                    allowed = true;
                    break;
                }
            }
        } catch (GHFileNotFoundException e) {
            log.error("설치 토큰을 찾을 수 없음", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(GitHubErrorCode.INSTALLATION_NOT_FOUND));
            return;

        } catch (Exception e) {
            log.error("[OAuth2Success] 설치 리포 조회/권한 확인 실패", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(GitHubErrorCode.GITHUB_API_ERROR));
            return;
        }

        if (!allowed) {
            log.info("[OAuth2Success] not collaborator on any installed repo. user={}", user.getGithubUsername());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(GitHubErrorCode.UNAUTHORIZED_REPOSITORY));
            return;
        }

        // 4) JWT 발급 → 헤더 반환
        String accessToken = jwtProvider.createAccessToken(
                Long.valueOf(user.getGithubId()), user.getGithubUsername()
        );

        Cookie jwtCookie = new Cookie("access_token", accessToken);
//        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour
        response.addCookie(jwtCookie);

        response.sendRedirect("http://localhost:3000/auth/callback?ok=true");

//        response.setHeader("Authorization", "Bearer " + accessToken);
//        response.setHeader("X-Username", user.getGithubUsername());
//        response.setHeader("Access-Control-Expose-Headers", "Authorization,X-Username");


    }
}