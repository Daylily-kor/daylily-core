package com.daylily.domain.auth.service;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User processOAuth2User(OAuth2User oAuth2User) {
        // 1. 파라미터로 들어온 값 중 githubId를 가져온다.
        Integer githubId = oAuth2User.getAttribute("id");

        return userRepository.findByGithubId(githubId)
                .orElseGet(() -> saveUserFormOAuth2(oAuth2User)); // 반환값이 없을 경우 지연실행
    }

    private User saveUserFormOAuth2(OAuth2User oAuth2User) {
        return userRepository.save(toUserEntity(oAuth2User));
    }

    private User toUserEntity(OAuth2User oAuth2User) {
        return User.builder()
                .githubId(oAuth2User.getAttribute("id"))
                .login(oAuth2User.getAttribute("login"))
                .email(oAuth2User.getAttribute("email"))
                .githubProfileUrl(oAuth2User.getAttribute("html_url"))
                .build();
    }
}
