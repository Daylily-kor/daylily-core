package com.daylily.domain.github.api;

import com.daylily.domain.github.entity.GitHubApp;
import org.kohsuke.github.GitHub;

public interface GitHubClientFactory {
    /**
     * User Access Token 방식으로 GitHub API에 접근합니다.
     * @param accessToken GitHub User Access Token
     */
    GitHub withOAuth(String accessToken);

    /**
     * GitHub App 자체로 인증하여 GitHub API에 접근합니다.<br/>
     * 사용 가능한 GitHub API: Installation Access Token 발급, 앱 관리
     * @param app GitHub 앱 <b>"등록" (설치가 아님!!)</b> 시 저장된 GitHubApp 객체
     */
    GitHub withApp(GitHubApp app);

    /**
     * GitHub App Installation 정보를 사용하여 GitHub API에 접근합니다.<br/>
     * 사용 가능한 GitHub API: 앱 설치 시 권한을 부여받은 모든 기능     *
     * @param app GitHub 앱 설치 후 `installation_id` 값이 갱신된 GitHubApp 객체
     */
    GitHub withAppInstallation(GitHubApp app);
}
