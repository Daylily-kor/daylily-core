package com.daylily.domain.docker.dto;

/**
 * gRPC 응답시 사용되는 DTO 클래스. Protobuf 파일로부터 컴파일된 Java 클래스로부터 맵핑됩니다.
 */
public abstract class GrpcResponse {

    // Swagger에서 GrpcDockerVersionResponse 표시 안되는 문제때문에 DTO 레코드 별도로 작성
    public record DockerVersionResponse(
            String version,
            String apiVersion,
            String platform,
            String os,
            String arch,
            String kernelVersion
    ) {

    }
}
