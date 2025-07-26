package com.daylily.global.exception;

import com.daylily.global.response.code.BaseCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final BaseCode errorCode;

    public BaseException(BaseCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }
}
