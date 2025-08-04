package com.daylily.global.config;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GitHubConfig {

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
