package com.daylily.domain.auth.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Integer githubId;
    private String login;
    private String email;
    private String githubProfileUrl;
}
