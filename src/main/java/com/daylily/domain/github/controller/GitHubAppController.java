package com.daylily.domain.github.controller;

import com.daylily.domain.github.dto.manifest.ManifestRequest;
import com.daylily.domain.github.dto.manifest.ManifestResponse;
import com.daylily.domain.github.service.GitHubAppService;
import com.daylily.global.response.SuccessResponse;
import com.daylily.global.util.StateStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@Tag(name = "GitHub App", description = "GitHub App Manifest 관련 API")
public class GitHubAppController {

    private final GitHubAppService service;
    private final StateStore stateStore;

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
        /*if (!stateStore.consume(state)) {
            throw new GitHubException(GitHubErrorCode.STATE_TOKEN_ERROR);
        }*/

        URI installURI = service.createGitHubApp(code);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(installURI)
                .build();
    }
}
