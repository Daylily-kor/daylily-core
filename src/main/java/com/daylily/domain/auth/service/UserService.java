package com.daylily.domain.auth.service;

import com.daylily.domain.auth.entity.User;
import com.daylily.domain.auth.entity.UserMapper;
import com.daylily.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User processOAuth2User(OAuth2User oAuth2User) {
        Object id = oAuth2User.getAttribute("id");
        // id가 Integer 또는 Long 클래스이어서 이렇게 처리함
        long githubId = switch (id) {
            case Long l -> l;
            case Integer i -> i.longValue();
            case null -> throw new IllegalArgumentException("GitHub ID is null");
            default -> throw new IllegalArgumentException("Unexpected type for GitHub ID: " + id.getClass());
        };
        return userRepository
                .findByGithubId(githubId) // 2. githubId로 User를 조회한다.
                .orElseGet(() -> userRepository.save(userMapper.toEntity(oAuth2User)));
    }
}
