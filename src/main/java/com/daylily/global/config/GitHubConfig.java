package com.daylily.global.config;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Configuration
public class GitHubConfig {

    // IAT(Install Access Token)을 가져오기 위한 WebClient 빌더
    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    @Bean
    public GitHub gh() {
        try {
            return new GitHubBuilder()
                    // TODO: OAuth 토큰 필요한지 잘 모르겠음. 일단 Anonymous 모드로 GitHub API 연결
//                    .withOAuthToken("")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
