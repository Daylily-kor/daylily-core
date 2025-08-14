package com.daylily.domain.github.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record InstallationAccessTokenRes(
        String token,
        @JsonProperty("expires_at") Instant expiresAt
        ) {
}
