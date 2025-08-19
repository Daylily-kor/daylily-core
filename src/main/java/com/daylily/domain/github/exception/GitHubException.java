package com.daylily.domain.github.exception;

import com.daylily.global.exception.BaseException;
import com.daylily.global.response.code.BaseCode;

public class GitHubException extends BaseException {
    public GitHubException(BaseCode errorCode) {
        super(errorCode);
    }

    public GitHubException(GitHubErrorCode errorCode, String customMessage) {
        super(new BaseCode() {
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
        });
    }
}
