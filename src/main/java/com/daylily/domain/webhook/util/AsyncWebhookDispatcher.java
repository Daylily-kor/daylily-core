package com.daylily.domain.webhook.util;

import com.daylily.domain.webhook.service.PullRequestHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("asyncWebhookDispatcher")
@RequiredArgsConstructor
public class AsyncWebhookDispatcher implements WebhookDispatcher {

    private final PullRequestHandlerService pullRequestHandlerService;

    @Override
    @Async("webhookExecutor")
    public void enqueue(String eventType, String rawPayload) {
        pullRequestHandlerService.handlePullRequestEvent(eventType, rawPayload);
    }
}
