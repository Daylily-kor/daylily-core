package com.daylily.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface BaseResponse permits SuccessResponse, ErrorResponse {
    int status(); // HTTP 상태 코드

    /**
     * 응답 바디를 {@link ResponseEntity}로 감쌉니다.
     */
    static <T extends BaseResponse> ResponseEntity<T> wrap(T body) {
        return ResponseEntity.status(body.status()).body(body);
    }
}
