package com.daylily.domain.webhook.controller;

import com.daylily.domain.webhook.util.WebhookDispatcher;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@Tag(name = "GitHub Webhook", description = "GitHub Webhook 요청 처리 API")
public class GitHubWebhookController {

    // TODO: 메시징 큐 버젼으로 변경 예정...?
    @Qualifier("asyncWebhookDispatcher")
    private final WebhookDispatcher webhookDispatcher;

    // Lombok `@RequiredArgsConstructor`은 코드 생성시 `@Qualifier`를 복사하지 않아 수동으로 생성자 작성
    public GitHubWebhookController(WebhookDispatcher webhookDispatcher) {
        this.webhookDispatcher = webhookDispatcher;
    }

    @PostMapping("/pull-request")
    public ResponseEntity<Void> onEvent(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestBody String rawPayload
    ) {
        if ("pull_request".equals(eventType)) {
            webhookDispatcher.enqueue(eventType, rawPayload);
        }
        return ResponseEntity.accepted().build();
    }
}
