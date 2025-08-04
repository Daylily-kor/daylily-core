package com.daylily.domain.auth.web.dto;

public record UserResponse(
        Integer githubId,
        String githubUsername,
        String email,
        String githubProfileUrl
) {}
