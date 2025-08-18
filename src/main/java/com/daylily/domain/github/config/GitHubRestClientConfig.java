package com.daylily.domain.github.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GitHubRestClientConfig {

    private static final String BASE_URL = "https://api.github.com";

    @Bean
    public RestClient gitHubRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(BASE_URL)
                .build();
    }
}
