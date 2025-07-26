package com.daylily.domain.docker.exception;

import com.daylily.global.response.code.BaseCode;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public record DockerGrpcErrorCode(String code, String message, HttpStatus httpStatus) implements BaseCode {

    @Override
    public int status() {
        return httpStatus.value();
    }

    /**
     * gRPC 예외로부터 가져온 `Status` 객체를 사용하여 에러 코드 생성
     * <ul>
     *     <li>코드: <code>DOCKER-GRPC-{예외-코드}</code></li>
     *     <li>메시지: <code>{예외-메시지} [Cause: 사유]</code></li>
     *     <li>HTTP 상태: <code>500 Internal Server Error</code></li>
     * </ul>
     */
    public static DockerGrpcErrorCode of(io.grpc.Status status) {
        var code = "DOCKER-GRPC-" + status.getCode().toString();

        String cause = Optional.ofNullable(status.getCause())
                .map(Throwable::getMessage)
                .orElse("No cause available");
        var description = "%s [Cause: %s]".formatted(status.getDescription(), cause);

        return new DockerGrpcErrorCode(code, description, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
