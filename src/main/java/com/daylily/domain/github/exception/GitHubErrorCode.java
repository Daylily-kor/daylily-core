package com.daylily.domain.github.exception;

import com.daylily.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum GitHubErrorCode implements BaseCode {

    INVALID_HEADER_SECRET("GITHUB-400-1", "Invalid or missing X-Hub-Signature", HttpStatus.BAD_REQUEST),
    SECRET_NOT_FOUND     ("GITHUB-400-2", "Webhook secret not configured",        HttpStatus.NOT_FOUND),
    APP_NOT_FOUND        ("GITHUB-400-3", "GitHub App not registered",              HttpStatus.NOT_FOUND),
    INVALID_EVENT_PAYLOAD("GITHUB-400-4", "Malformed webhook payload",             HttpStatus.BAD_REQUEST),

    GITHUB_API_ERROR                 ("GITHUB-500-1", "Error calling GitHub API",              HttpStatus.BAD_GATEWAY),
    GITHUB_SECRET_VERIFICATION_FAILED("GITHUB-500-2", "Webhook signature verification failed", HttpStatus.UNAUTHORIZED),
    STATE_TOKEN_ERROR                ("GITHUB-500-3", "State token invalid or expired",        HttpStatus.UNAUTHORIZED),
    GITHUB_LIST_REPOS_ERROR          ("GITHUB-500-4", "Error listing repositories from GitHub", HttpStatus.BAD_GATEWAY),;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int status() {
        return httpStatus.value();
    }
}
