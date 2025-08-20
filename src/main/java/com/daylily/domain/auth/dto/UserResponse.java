package com.daylily.domain.auth.dto;

public record UserResponse(
        Integer githubId,
        String githubUsername,
        String email,
        String githubProfileUrl
) {}
