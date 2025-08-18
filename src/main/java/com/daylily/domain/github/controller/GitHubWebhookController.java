package com.daylily.domain.github.controller;

import com.daylily.domain.github.util.WebhookDispatcher;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@Tag(name = "GitHub Webhook", description = "GitHub Webhook 처리 API")
public class GitHubWebhookController {

    // TODO: 메시징 큐 버젼으로 변경 예정...?
    @Qualifier("asyncWebhookDispatcher")
    private final WebhookDispatcher webhookDispatcher;

    // Lombok `@RequiredArgsConstructor`은 코드 생성시 `@Qualifier`를 복사하지 않아 수동으로 생성자 작성
    public GitHubWebhookController(WebhookDispatcher webhookDispatcher) {
        this.webhookDispatcher = webhookDispatcher;
    }

    @PostMapping
    public ResponseEntity<Void> onEvent(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestBody String payload
    ) {
        // 이하 WebhookRequestFilter 에서 요청 헤더의 "x-hub-signature-256" 해시가 검증이 완료된 상태
        switch (eventType) {
            case "pull_request" -> webhookDispatcher.enqueue(eventType, payload);
            default             -> log.debug("Unhandled GitHub event type: {}", eventType);
        }

        return ResponseEntity.accepted().build();
    }
}
