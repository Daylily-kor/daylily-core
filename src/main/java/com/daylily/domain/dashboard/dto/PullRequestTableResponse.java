package com.daylily.domain.dashboard.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

public record PullRequestTableResponse(
        int totalRepositoryCount,
        int totalPullRequestCount, // Added total PR count across all repositories
        Map<String, List<PullRequestTableRow>> pullRequestsByRepository, // Renamed for clarity
        Map<String, ContainerStatusSummary> containerStatusSummary // Added summary by repository
) {
    @Builder
    public record ContainerStatusSummary(
            int created,
            int running,
            int paused,
            int exited,
            int dead
    ) {}
}