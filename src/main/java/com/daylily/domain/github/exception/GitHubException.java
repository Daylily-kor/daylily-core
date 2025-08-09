package com.daylily.domain.github.exception;

import com.daylily.global.exception.BaseException;
import com.daylily.global.response.code.BaseCode;

public class GitHubException extends BaseException {
    public GitHubException(BaseCode errorCode) {
        super(errorCode);
    }
}
