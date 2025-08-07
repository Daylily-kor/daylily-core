package com.daylily.domain.github.service;

public interface PullRequestHandlerService {

    void handlePullRequestEvent(String eventType, String rawPayload);
}
