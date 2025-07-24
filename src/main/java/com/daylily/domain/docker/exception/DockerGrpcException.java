package com.daylily.domain.docker.exception;

import com.daylily.global.exception.BaseException;
import com.daylily.global.response.code.BaseCode;

/**
 * Daylily gRPC 서버와 통신 중 발생하는 예외 클래스
 */
public class DockerGrpcException extends BaseException {

    public DockerGrpcException(BaseCode errorCode) {
        super(errorCode);
    }
}
