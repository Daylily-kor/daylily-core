package com.daylily.global.response.code;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode implements BaseCode {

    // Global Error Codes
    // 4xx
    BAD_REQUEST("GLOBAL-400-1", "Bad request", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("GLOBAL-400-2", "Malformed request", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("GLOBAL-403", "Access denied", HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED("GLOBAL-405", "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),

    // 5xx
    INTERNAL_SERVER_ERROR("GLOBAL-500", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int status() {
        return httpStatus.value();
    }
}
