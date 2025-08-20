package com.daylily.domain.auth.handler;

import com.daylily.domain.auth.service.UserService;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.service.GitHubAppAuthService;
import com.daylily.global.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GitHubAppAuthService gitHubAppAuthService;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Value("${daylily.login.redirect-url}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        GitHubAppAuthService.AuthResult result = gitHubAppAuthService.authenticateGitHubUser(oAuth2User);

        if (result.success()) {
//            response.addCookie(result.jwtCookie());
//            response.setHeader("Access-Control-Allow-Credentials", "true");
//            response.setHeader("Access-Control-Allow-Origin", "*");
            response.sendRedirect(redirectUri + "?state=" + result.state().toString());
        }
        else {
            log.error("[OAuth2AuthenticationSuccessHandler] 인증 실패: {}", result.errorMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(GitHubErrorCode.UNAUTHORIZED_REPOSITORY, result.errorMessage()));
        }
    }
}