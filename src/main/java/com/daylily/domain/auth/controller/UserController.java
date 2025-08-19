package com.daylily.domain.auth.controller;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.entity.UserMapper;
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

    private final UserMapper userMapper;

    @GetMapping("/information")
    public ResponseEntity<UserResponse> getCurrentUserInformation(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
