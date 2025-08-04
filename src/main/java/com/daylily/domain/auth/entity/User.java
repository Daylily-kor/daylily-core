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

    private String githubUsername; // 원래는 login 이라는 이름인데, Mapper로 인해 Entity명 명확하게 지정
    private String email;
    private String githubProfileUrl; // https://github.com/{username}

    @Builder
    public User(Integer githubId, String githubUsername, String email, String githubProfileUrl) {
        this.githubId = githubId;
        this.githubUsername = githubUsername;
        this.email = email;
        this.githubProfileUrl = githubProfileUrl;
    }


}
