package com.daylily.domain.dashboard.service;

import com.daylily.domain.dashboard.dto.PullRequestTableRow;

import java.util.List;
import java.util.Map;

public interface PullRequestTableService {
    Map<String, List<PullRequestTableRow>> getPullRequests();
}
