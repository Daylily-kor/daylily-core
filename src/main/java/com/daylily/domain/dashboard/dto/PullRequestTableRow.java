package com.daylily.domain.dashboard.dto;

import lombok.Builder;

import java.net.URI;

@Builder
public record PullRequestTableRow(
        URI url,
        String title,
        int number,
        String author,
        String state, // open, closed, all
        String createdAt,
        String updatedAt,
        String containerUrl,
        ContainerStatusType containerState, // "created", "running", "paused", "restarting", "exited", "removing", "dead"
        String containerStatus // Additional human-readable status of this container
) {
}
