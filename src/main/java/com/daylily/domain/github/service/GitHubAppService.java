package com.daylily.domain.github.service;

import com.daylily.domain.github.dto.manifest.ManifestRequest;
import com.daylily.domain.github.dto.manifest.ManifestResponse;

import java.net.URI;

public interface GitHubAppService {

    ManifestResponse createManifest(ManifestRequest manifestRequest);

    /**
     * GitHub Manifest flow로 부터 받은 임시 코드를 사용하여 사용자 계정에 GitHub App을 등록합니다.
     * @param code GitHub Manifest flow로부터 받은 임시 코드
     * @return <code>https://github.com/apps/{app-name}/installations/new</code> GitHub App 설치 페이지 URI를 반환합니다.
     */
    URI createGitHubApp(String code);

    /**
     * GitHub App 설치 시 발생하는 {@code installation} 웹훅을 처리합니다.
     * @param rawPayload 웹훅 페이로드
     */
    void handleGitHubAppInstallation(String rawPayload);
}
