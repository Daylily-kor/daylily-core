package com.daylily.domain.github.controller;

import com.daylily.domain.github.dto.manifest.ManifestRequest;
import com.daylily.domain.github.dto.manifest.ManifestResponse;
import com.daylily.domain.github.service.GitHubAppAuthService;
import com.daylily.domain.github.service.GitHubAppInstallationService;
import com.daylily.domain.github.service.GitHubAppService;
import com.daylily.global.response.SuccessResponse;
import com.daylily.global.util.StateStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@Tag(name = "GitHub App", description = "GitHub App Manifest 관련 API")
public class GitHubAppController {

    private final GitHubAppService service;
    private final StateStore stateStore;
    private final GitHubAppAuthService gitHubAppAuthService;
    private final GitHubAppInstallationService gitHubAppInstallationService;

    @Value("${daylily.login.redirect-url}")
    private String redirectUri;

    @Operation(
            summary = "GitHub App Manifest JSON 생성",
            description = """
                    GitHub App Manifest JSON을 생성합니다.<br/>
                    응답에서 `manifest`는 요청 바디에 `"manifest": { ... }`로 포함하고, `state` 값은 쿼리 파라미터로 `https://github.com/settings/apps/new?state={state}` URL로 POST 요청을 보내면(`<form>` 태그 사용)
                    <a href="https://docs.github.com/en/apps/sharing-github-apps/registering-a-github-app-from-a-manifest" target="_blank">GitHub App Manifest flow</a>가 시작됩니다.<br/>
                    """
    )
    @PostMapping("/manifest")
    public ResponseEntity<SuccessResponse<ManifestResponse>> createGitHubAppManifest(
            @Valid @RequestBody ManifestRequest manifestRequest
    ) {
        ManifestResponse manifestResponse = service.createManifest(manifestRequest);
        return SuccessResponse.ok(manifestResponse);
    }

    @GetMapping("/manifest/redirect")
    public ResponseEntity<Void> createGitHubApp(
            @RequestParam("state") String state,
            @RequestParam("code") String code
    ) {
        // CSRF 공격 방지를 위해 상태 토큰을 검증
        // TODO: 추후 재활성화
        /*if (!stateStore.consume(state)) {
            throw new GitHubException(GitHubErrorCode.STATE_TOKEN_ERROR);
        }*/

        URI installURI = service.createGitHubApp(code);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(installURI)
                .build();
    }

    /**
     * GitHub App 설치 후 사용자가 바로 로그인하여 JWT를 받을 수 있도록 하는 엔드포인트
     */
    @GetMapping("/install/oauth/callback")
    public void handleInstallationOauthCallback(
            @RequestParam                           String code,
            @RequestParam(name = "installation_id") Long installationId,
            @RequestParam(name = "setup_action")    String setupAction,
            HttpServletResponse response
    ) throws IOException {
        log.debug("[GitHubAppController] GitHub App 설치 후 OAuth2 인증 요청: code={}, installationId={}, setupAction={}",
                code, installationId, setupAction);

        // 1. GitHub 앱 설치 후 발급받은 installation_id를 저장
        // 앱 등록 과정에서 Client ID, Client Secret, Redirect URI 등을 설정하고, 설치 후 GitHub가 이 엔드포인트로 리디렉트,
        // 이후 installation_id를 사용하여 GitHub 앱의 설치 정보를 업데이트.
        gitHubAppInstallationService.updateInstallationId(installationId);

        // 2. 수동으로 GitHub로부터 액세스 토큰을 발급, 토큰을 통해 GitHub 유저 정보 로드
        OAuth2User oAuth2User = gitHubAppAuthService.exchangeAccessToken(code);

        // 3. 현재 앱을 설치 후 로그인 중인 유저가 GitHub 앱이 설치된 저장소들의 Collaborator 인지 확인(당연함)
        GitHubAppAuthService.AuthResult result = gitHubAppAuthService.authenticateGitHubUser(oAuth2User);

        log.debug("[GitHubAppController] GitHub App 설치 후 OAuth2 인증 결과: {}", result);
        log.debug("[GitHubAppController] redirect user with jwt");

        // 인증 성공 시 Spring boot에서 발급하는 JWT 토큰을 쿠키에 저장하고 리다이렉트
        if (result.success()) {
            response.sendRedirect(redirectUri + "?state=" + result.state().toString());
        }
        else {
            // 인증 실패 시 에러 메시지를 클라이언트에 전달
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
