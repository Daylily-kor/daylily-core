package com.daylily.global.exception;

import com.daylily.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {
    private final BaseCode errorCode;
}
