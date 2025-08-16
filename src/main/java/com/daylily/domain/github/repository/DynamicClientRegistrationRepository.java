package com.daylily.domain.github.repository;


import com.daylily.domain.github.entity.GitHubApp;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

/**
 * DB에 저장된 GitHub App 자격증명(client_id/secret)을
 * 런타임 시 읽어 OAuth2 ClientRegistration을 동적으로 생성
 *
 * properties/.env 하드코딩 불필요
 * 최신(updatedAt DESC) 1건만 사용
 * 팀장 1명만 github_app table에 등록되는 거니까.. 걍 Spring data jpa 신경안씀
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private static final String REGISTRATION_ID = "github-app";

    private final GitHubAppRepository repo;

    /**
     * Spring Security가 OAuth2 로그인 시 이 메서드를 호출해
     * 주어진 registrationId에 해당하는 ClientRegistration을 요청한다.
     */
    @Override
    @Transactional
    public ClientRegistration findByRegistrationId(String registrationId) {
        log.debug("[OAuth] findByRegistrationId called: {}", registrationId);
        if (!REGISTRATION_ID.equals(registrationId)) {
            return null;
        }

        // 최신 1건 조회
        GitHubApp app = repo.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("GitHub App이 아직 설치/연결되지 않았습니다."));

        // GitHub App의 user-to-server OAuth는 OAuth Apps와 동일 엔드포인트 사용
        return ClientRegistration.withRegistrationId(REGISTRATION_ID)
                .clientId(app.getClientId())
                .clientSecret(app.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("read:user")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub App")
                .build();

    }

}
