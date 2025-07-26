package com.daylily.global.response.code;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum SuccessCode implements BaseCode {

    // Global Success Codes
    OK("GLOBAL-200", "Request successful", HttpStatus.OK),
    CREATED("GLOBAL-201", "Resource created", HttpStatus.CREATED),
    NO_CONTENT("GLOBAL-204", "No content", HttpStatus.NO_CONTENT)
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
