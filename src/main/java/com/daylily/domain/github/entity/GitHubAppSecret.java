package com.daylily.domain.github.entity;

import com.daylily.global.entity.AesGcmStringConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_app_secret")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GitHubAppSecret {
    @Id
    private Long id;

    private String clientId;

    @Convert(converter = AesGcmStringConverter.class)
    private String clientSecret;

    @Convert(converter = AesGcmStringConverter.class)
    private String webhookSecret;

    @Lob
    @Convert(converter = AesGcmStringConverter.class)
    private String pem;

    @Setter
    private Long installationId;
}
