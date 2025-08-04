package com.daylily.domain.auth.handler;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.service.UserService;
import com.daylily.global.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // 1.  서비스 계층 위임
        // 1-1 사용자 정보 추출
        // 1-2 DB 저장 또는 조회
        User user = userService.processOAuth2User(oAuth2User);
        // 2.  JWT 생성
        String accessToken = jwtProvider.createAccessToken(Long.valueOf(user.getGithubId()), user.getGithubUsername());
        // 3.  JSON 응답 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", accessToken);
        responseBody.put("username", user.getGithubUsername());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(json);
    }

}
