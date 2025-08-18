package com.daylily.domain.github.service;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.service.UserService;
import com.daylily.domain.github.entity.GitHubApp;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import com.daylily.domain.github.api.GitHubClient;
import com.daylily.global.jwt.JwtProvider;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubAppAuthService {

    public record AuthResult(
            boolean success,
            Cookie jwtCookie,
            String errorMessage
    ) {
        public static AuthResult success(Cookie jwtCookie) {
            return new AuthResult(true, jwtCookie, null);
        }

        public static AuthResult failure(String errorMessage) {
            return new AuthResult(false, null, errorMessage);
        }
    }

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final GitHubAppRepository gitHubAppRepository;
    private final GitHubClient gitHubClient;
    private final RestClient gitHubRestClient;

    @Transactional
    public AuthResult authenticateGitHubUser(OAuth2User oAuth2User) {
        User user = userService.processOAuth2User(oAuth2User);

        if (!isCollaboratorOfApp(user)) {
            log.error("[GitHubAppAuthService] 사용자 {}는 GitHub App의 협업자가 아닙니다.", user.getGithubUsername());
            return AuthResult.failure("사용자 %s는 이 GitHub App의 협업자가 아닙니다.".formatted(user.getGithubUsername()));
        }

        String accessToken = jwtProvider.createAccessToken(user.getGithubId(), user.getGithubUsername());

        Cookie jwtCookie = new Cookie("DAYLILY_JWT", accessToken);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour
        jwtCookie.setHttpOnly(false);
        return AuthResult.success(jwtCookie);
    }

    @Transactional(readOnly = true)
    public OAuth2User exchangeAccessToken(String code) {
        var app = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", app.getClientId());
        params.add("client_secret", app.getClientSecret());
        params.add("code", code);

        var tokenResponse = gitHubRestClient
                .post()
                .uri("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub User Access Token 발급 중 오류 발생");
                })
                .body(JsonNode.class);

        if (tokenResponse == null || !tokenResponse.has("access_token")) {
            log.error("[GitHubAppAuthService] GitHub Access Token 발급 실패: 응답이 유효하지 않습니다.");
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub Access Token 발급 실패: 응답이 유효하지 않습니다.");
        }

        String accessToken = tokenResponse.path("access_token").asText();
        String expiresIn = tokenResponse.path("expires_in").asText();
        String scope = tokenResponse.path("scope").asText();
        log.debug("[GitHubAppAuthService] GitHub User Access Token 발급 성공: expiresIn={}, scope={}", expiresIn, scope);

        GitHub gh = gitHubClient.withOAuth(accessToken);
        GHUser ghUser;
        String email;
        try {
            ghUser = gh.getMyself();
            // 계정 설정에서 이메일 공개로 되어있지 않으면 null 값이 날라옴.
            email = Optional.ofNullable(ghUser.getEmail()).orElse("");
        } catch (IOException e) {
            log.error("[GitHubAppAuthService] GitHub 사용자 정보 조회 중 오류 발생: {}", e.getMessage());
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub 사용자 정보 조회 중 오류 발생");
        }

        Map<String, Object> attributes = Map.of(
                "id",       ghUser.getId(),
                "login",    ghUser.getLogin(),
                "html_url", ghUser.getHtmlUrl().toString(),
                "email",    email
        );

        return new DefaultOAuth2User(Collections.emptyList(), attributes, "id");
    }

    @Transactional(readOnly = true)
    protected boolean isCollaboratorOfApp(User user) {
        GitHubApp app = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        if (app.getInstallationId() == null) {
            log.error("[GitHubAppAuthService] collaborator 확인 도중, GitHub App 설치 ID가 설정되어 있지 않습니다.");
            throw new GitHubException(GitHubErrorCode.INSTALLATION_NOT_FOUND, "Collaborator 확인 도중, GitHub App 설치 ID가 설정되어 있지 않습니다.");
        }

        GitHub gh = gitHubClient.withAppInstallation(app);
        GHUser ghUser;
        try {
            ghUser = gh.getUser(user.getLogin());
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub 사용자 정보 조회 중 오류 발생: " + e.getMessage());
        }

        List<GHRepository> repositories;
        try {
            repositories = gh.getInstallation().listRepositories().toList();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub 저장소 목록 조회 중 오류 발생: " + e.getMessage());
        }

        if (repositories.isEmpty()) {
            log.error("[GitHubAppAuthService] 협업자 확인 도중, GitHub 저장소 목록이 비어 있습니다.");
            throw new GitHubException(GitHubErrorCode.GITHUB_LIST_REPOS_ERROR, "협업자 확인 도중, GitHub 저장소 목록이 비어 있습니다.");
        }

        return repositories.stream()
                .anyMatch(repository -> this.isCollaborator(ghUser, repository));
    }

    private boolean isCollaborator(GHUser ghUser, GHRepository repository) {
        try {
            return repository.isCollaborator(ghUser);
        } catch (IOException e) {
            log.error("[GitHubAppAuthService] 저장소 {}에서 협업자 확인 중 오류 발생: {}", repository.getFullName(), e.getMessage());
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "협업자 확인 중 오류 발생: " + e.getMessage());
        }
    }
}
