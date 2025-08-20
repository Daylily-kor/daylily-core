package com.daylily.domain.github.api;

import com.daylily.domain.github.entity.GitHubApp;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.global.exception.BaseException;
import com.daylily.global.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubClientImpl implements GitHubClientFactory {

    @Override
    public GitHub withOAuth(String accessToken) {
        try {
            return new GitHubBuilder().withOAuthToken(accessToken).build();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub API 연결 실패: " + e.getMessage());
        }
    }

    @Override
    public GitHub withApp(GitHubApp app) {
        if (app.getAppId() == null || app.getPem() == null) {
            throw new GitHubException(GitHubErrorCode.APP_NOT_FOUND, "GitHub API 연결에 필요한 정보 누락");
        }

        try {
            var jwtTokenProvider = new JWTTokenProvider(String.valueOf(app.getAppId()), app.getPem());
            // Bearer eyJhbGciOiJSUzI1NiJ9... 이기에 뒷부분만 잘라내서 씀
            String jwt = jwtTokenProvider.getEncodedAuthorization().split(" ")[1];
            return new GitHubBuilder().withJwtToken(jwt).build();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub API 연결 실패: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to create JWTTokenProvider");
        }
    }

    @Override
    public GitHub withAppInstallation(GitHubApp app) {
        if (app.getAppId() == null || app.getPem() == null || app.getInstallationId() == null) {
            throw new GitHubException(GitHubErrorCode.APP_NOT_FOUND, "GitHub API 연결에 필요한 정보 누락");
        }

        GitHub ghWithApp = this.withApp(app);

        try {
            GHAppInstallation installation = ghWithApp.getApp().getInstallationById(app.getInstallationId());
            String installationToken = installation.createToken().create().getToken();
            return new GitHubBuilder().withAppInstallationToken(installationToken).build();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR, "GitHub API 연결 실패: " + e.getMessage());
        }
    }
}
