package com.daylily.domain.dashboard.controller;

import com.daylily.domain.dashboard.dto.ContainerStatusType;
import com.daylily.domain.dashboard.dto.PullRequestTableResponse;
import com.daylily.domain.dashboard.dto.PullRequestTableRow;
import com.daylily.domain.dashboard.service.PullRequestTableService;
import com.daylily.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PullRequestTableService pullRequestTableService;

    @GetMapping("/pull-requests")
    public ResponseEntity<SuccessResponse<PullRequestTableResponse>> getPullRequests() {
        Map<String, List<PullRequestTableRow>> pullRequestsMap = pullRequestTableService.getPullRequests();
        PullRequestTableResponse response = buildPullRequestTableResponse(pullRequestsMap);

        return SuccessResponse.ok(response);
    }

    private PullRequestTableResponse buildPullRequestTableResponse(Map<String, List<PullRequestTableRow>> pullRequestsMap) {
        int totalPullRequestCount = pullRequestsMap.values().stream()
                .mapToInt(List::size)
                .sum();

        Map<String, PullRequestTableResponse.ContainerStatusSummary> statusSummary = pullRequestsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateContainerStatusSummary(entry.getValue())
                ));

        return new PullRequestTableResponse(
                pullRequestsMap.size(),
                totalPullRequestCount,
                pullRequestsMap,
                statusSummary
        );
    }

    private PullRequestTableResponse.ContainerStatusSummary calculateContainerStatusSummary(List<PullRequestTableRow> rows) {
        Map<ContainerStatusType, Long> statusCounts = rows.stream()
                .collect(Collectors.groupingBy(
                        PullRequestTableRow::containerState,
                        Collectors.counting()
                ));

        return PullRequestTableResponse.ContainerStatusSummary.builder()
                .created(statusCounts.getOrDefault(ContainerStatusType.CREATED, 0L).intValue())
                .running(statusCounts.getOrDefault(ContainerStatusType.RUNNING, 0L).intValue())
                .paused(statusCounts.getOrDefault(ContainerStatusType.PAUSED, 0L).intValue())
                .exited(statusCounts.getOrDefault(ContainerStatusType.EXITED, 0L).intValue())
                .dead(statusCounts.getOrDefault(ContainerStatusType.DEAD, 0L).intValue())
                .build();
    }
}
