package com.daylily.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(unique = true, nullable = false)
    private Long githubId;

    private String githubUsername; // 원래는 login 이라는 이름인데, Mapper로 인해 Entity명 명확하게 지정
    private String email;
    private String githubProfileUrl; // https://github.com/{username}

    @Transient
    public String getLogin() {
        return githubUsername;
    }
}
