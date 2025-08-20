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

    public BaseException(BaseCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = new BaseCode() {
            @Override
            public String code() {
                return errorCode.code();
            }

            @Override
            public String message() {
                return customMessage;
            }

            @Override
            public int status() {
                return errorCode.status();
            }
        };
    }
}
