package com.daylily.global.config;

import com.daylily.domain.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.daylily.domain.auth.service.UserService;
import com.daylily.global.exception.CustomAccessDeniedHandler;
import com.daylily.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // API만 쓰는 동안엔 세션 끔
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/error",
                                "/health", "/actuator/**",
                                "/api/app/manifest/**",
                                "/api/app/callback",
                                "/api/webhook",
                                // IAT 발급 테스트(임시 허용)
                                "/api/app/installation-token"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 미인증 시 리다이렉트 말고 401 JSON을 반환
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // 브라우저에서 실제 OAuth2 로그인 쓸 때만 동작
                .oauth2Login(oauth -> oauth.successHandler(oAuth2AuthenticationSuccessHandler()))
                // 불필요한 기본 폼/로그아웃 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(userService, jwtProvider);
    }
}

