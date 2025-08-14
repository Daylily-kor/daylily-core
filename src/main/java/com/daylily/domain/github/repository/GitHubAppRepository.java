package com.daylily.domain.github.repository;

import com.daylily.domain.github.entity.GitHubApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GitHubAppRepository extends JpaRepository<GitHubApp, Long> {

    Optional<GitHubApp> findByAppId(Long appId);

    Optional<GitHubApp> findByInstallationId(long installationId);
}
