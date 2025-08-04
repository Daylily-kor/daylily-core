package com.daylily.domain.auth.web.controller;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.service.UserService;
import com.daylily.domain.auth.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/information")
    public ResponseEntity<UserResponse> getCurrentUserInformation(@AuthenticationPrincipal User user) {
        UserResponse dto = new UserResponse(
                user.getGithubId(),
                user.getLogin(),
                user.getGithubProfileUrl(),
                user.getEmail()
        );
        return ResponseEntity.ok(dto);
    }
}
