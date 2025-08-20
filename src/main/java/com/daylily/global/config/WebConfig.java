package com.daylily.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@PropertySource("classpath:daylily.properties")
public class WebConfig implements WebMvcConfigurer {

    @Value("${daylily.login.redirect-url}")
    private String loginRedirectUri;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // remove /auth/callback from http://127.0.0.1:3000/auth/callback
        String frontendUrl = loginRedirectUri.replace("/auth/callback", "");

        registry.addMapping("/**") // 모든 경로
                .allowedOriginPatterns(frontendUrl)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")        // GET, POST, PUT, DELETE, OPTIONS 등 모든 메서드
                .allowedHeaders("*")
                .exposedHeaders("Set-Cookie")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
