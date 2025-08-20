package com.daylily.domain.github.service;

import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubAppInstallationService {

    private final GitHubAppRepository repository;

    @Transactional
    public void updateInstallationId(Long installationId) {
        if (installationId == null) {
            throw new IllegalArgumentException("Installation ID must not be null");
        }

        repository
                .findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND))
                .setInstallationId(installationId);
    }
}
