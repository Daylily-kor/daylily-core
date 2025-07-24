package com.daylily.domain.webhook.util;

// TODO: 메시징 큐를 흉내낸 인터페이스이므로, 추후 메시징 큐 도입 시 코드 수정 예정
public interface WebhookDispatcher {

    void enqueue(String eventType, String rawPayload);
}
