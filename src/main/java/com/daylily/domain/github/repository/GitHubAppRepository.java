package com.daylily.domain.github.repository;

import com.daylily.domain.github.entity.GitHubAppSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubAppRepository extends JpaRepository<GitHubAppSecret, Long> {
}
