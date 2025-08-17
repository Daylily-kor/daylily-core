package com.daylily.domain.github.service;

import com.daylily.domain.github.action_type.InstallationActionType;
import com.daylily.domain.github.dto.GitHubAppMapper;
import com.daylily.domain.github.dto.manifest.Manifest;
import com.daylily.domain.github.dto.manifest.ManifestRequest;
import com.daylily.domain.github.dto.manifest.ManifestResponse;
import com.daylily.domain.github.entity.GitHubApp;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import com.daylily.domain.github.util.ActionTypeChecker;
import com.daylily.domain.github.util.PayloadParser;
import com.daylily.global.config.GitHubConfig;
import com.daylily.global.config.GithubJwtSigner;
import com.daylily.global.util.StateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHAppFromManifest;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubAppServiceImpl implements GitHubAppService {

    private final GitHubAppRepository repository;

    private final StateStore stateStore;
    private final PayloadParser payloadParser;
    private final GitHubAppMapper mapper;

    private final GitHubConfig.GitHubClients gh;
    private final GithubJwtSigner jwtSigner;

    @Override
    public ManifestResponse createManifest(ManifestRequest manifestRequest) {
        var manifest = Manifest.withManifestRequest(manifestRequest);

        // CSRF 공격 방지를 위해 상태 토큰을 생성하고 저장
        var state = UUID.randomUUID().toString();
        stateStore.save(state, Duration.ofMinutes(10)); // 유효 시간 10분 넘겨준 것 의미 없음(기본값 10분 대신 사용)

        return new ManifestResponse(state, manifest);
    }

    @Transactional
    @Override
    public URI createGitHubApp(String code) {
        // GitHub Manifest flow로부터 받은 임시 코드를 사용하여 GitHub App을 생성
        GHAppFromManifest app;
        try {
            app = GitHub.connectAnonymously().createAppFromManifest(code);
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR);
        }

        log.debug("GitHub App created: {}", app.getSlug());
        log.debug("- GitHub App ID: {}", app.getId());
        log.debug("- GitHub App Client ID: {}", app.getClientId());

        GitHubApp appEntity = mapper.toEntity(app);
        repository.save(appEntity);

        var redirectUri = URI.create("https://github.com/apps/%s/installations/new".formatted(app.getSlug()));
        log.debug("Redirect URI for GitHub App installation: {}", redirectUri);

        return redirectUri;
    }

    @Transactional
    @Override
    public void handleGitHubAppInstallation(String rawPayload) {
        GHEventPayload.Installation installationPayload = payloadParser.parseInstallation(rawPayload);
        InstallationActionType actionType = ActionTypeChecker.fromInstallationActionString(installationPayload.getAction());

        if (actionType != InstallationActionType.CREATED) {
            log.debug("Unhandled GitHub App installation action: {}", actionType);
            return; // Only handle 'created' action
        }

        GHAppInstallation installation = installationPayload.getInstallation();
        long installationId = installation.getId();
        long appId = installation.getAppId();

        GitHubApp appSecret = repository.findByAppId(appId)
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        appSecret.setInstallationId(installationId);
    }

    public boolean isCollaborator(String owner, String repo, String githubLogin) {
        var app = repository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("GitHub App 미구성"));

        // 한 줄로: 발급 + 설치 토큰 + 체크
        return gh.hasReadAccess(app.getAppId(), app.getPem(), owner, repo, githubLogin);
    }
}
