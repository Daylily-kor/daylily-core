package com.daylily.domain.auth.web.dto;

public record UserResponse(
        Integer githubId,
        String login,
        String email,
        String githubProfileUrl
) {}
