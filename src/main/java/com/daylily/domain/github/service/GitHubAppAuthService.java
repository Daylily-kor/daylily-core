package com.daylily.domain.github.service;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.service.UserService;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import com.daylily.global.config.GitHubClient;
import com.daylily.global.jwt.JwtProvider;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
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
            User user,
            String jwt,
            String errorMessage
    ) {}

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final GitHubAppRepository gitHubAppRepository;
    private final GitHubClient gitHubClient;
    private final RestClient gitHubRestClient;

    public AuthResult authenticateGitHubUser(OAuth2User oAuth2User, HttpServletResponse response) throws IOException {
        User user = userService.processOAuth2User(oAuth2User);
        boolean isCollaborator = isCollaborator(user);

        if (!isCollaborator) {
            log.error("[GitHubAppAuthService] 사용자 {}는 GitHub App의 협업자가 아닙니다.", user.getGithubUsername());
            return new AuthResult(false, user, null, "사용자는 GitHub App의 협업자가 아닙니다.");
        }

        log.info("[GitHubAppAuthService] 사용자 {}는 GitHub App의 협업자입니다.", user.getGithubUsername());

        String accessToken = jwtProvider.createAccessToken(
                Long.valueOf(user.getGithubId()), user.getGithubUsername()
        );

        Cookie jwtCookie = new Cookie("access_token", accessToken);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour
        jwtCookie.setHttpOnly(false);
        response.addCookie(jwtCookie);

        return new AuthResult(true, user, accessToken, null);
    }

    public OAuth2User exchangeAccessToken(String code) {
        var app = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", app.getClientId());
        params.add("client_secret", app.getClientSecret());
        params.add("code", code);

        var tokenResponse = gitHubRestClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .body(params)
                .retrieve()
                .body(JsonNode.class);

        assert tokenResponse != null;
        String accessToken = tokenResponse.path("access_token").asText();
        log.debug("[GitHubAppAuthService] GitHub Access Token: OK");

        try {
            GitHub gh = new GitHubBuilder().withOAuthToken(accessToken).build();
            GHUser ghUser = gh.getMyself();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", ghUser.getId());
            attributes.put("login", ghUser.getLogin());
            attributes.put("html_url", ghUser.getHtmlUrl());
            attributes.put("email", Optional.ofNullable(ghUser.getEmail()).orElse(""));

            return new DefaultOAuth2User(Collections.emptyList(), attributes, "id");
        } catch (IOException e) {
            log.error("[GitHubAppAuthService] GitHub 사용자 정보 조회 중 오류 발생: {}", e.getMessage());
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR);
        }
    }

    private boolean isCollaborator(User user) {
        var app = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        var gh = gitHubClient.asInstallationFromGitHubAppEntity(app);

        GHUser ghUser;
        try {
            ghUser = gh.getUser(user.getGithubUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.debug("[GitHubAppAuthService] GitHub 사용자 정보: {}", ghUser.getLogin());

        List<GHRepository> repositories;
        try {
            repositories = gh.getInstallation().listRepositories().toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (GHRepository repository : repositories) {
            try {
                if (repository.isCollaborator(ghUser)) {
                    return true;
                }
            } catch (IOException e) {
                log.error("[GitHubAppAuthService] Repository {}에서 협업자 확인 중 오류 발생: {}", repository.getFullName(), e.getMessage());
            }
        }

        return false;
    }
}
