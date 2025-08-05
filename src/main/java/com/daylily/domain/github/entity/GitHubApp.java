package com.daylily.domain.github.entity;

import com.daylily.global.entity.AesGcmStringConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_app")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GitHubApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    @Convert(converter = AesGcmStringConverter.class)
    private String clientSecret;

    @Convert(converter = AesGcmStringConverter.class)
    private String webhookSecret;

    @Lob
    @Convert(converter = AesGcmStringConverter.class)
    private String pem;

    private Long appId;

    @Setter
    private Long installationId;
}
