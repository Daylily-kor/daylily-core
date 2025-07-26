package com.daylily.domain.webhook.service;

public interface PullRequestHandlerService {

    void handlePullRequestEvent(String eventType, String rawPayload);
}
