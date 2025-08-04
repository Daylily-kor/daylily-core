package com.daylily.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
// 외부에서 new User()을 막아두기 위해 AccessLevel.PROTECTED 적용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer githubId;

    private String login; // username
    private String email;
    private String githubProfileUrl; // https://github.com/{username}

    @Builder
    public User(Integer githubId, String login, String email, String githubProfileUrl) {
        this.githubId = githubId;
        this.login = login;
        this.email = email;
        this.githubProfileUrl = githubProfileUrl;
    }


}
