package com.daylily.domain.docker.dto;

/**
 * gRPC 응답시 사용되는 DTO 클래스. Protobuf 파일로부터 컴파일된 Java 클래스로부터 맵핑됩니다.
 */
public abstract class GrpcResponse {

    public record BuildImageResponse(
            String imageId,
            String imageName,
            String imageTag
    ) {

    }

    public record RunContainerResponse(
            String containerId,
            String containerName,
            String status
    ) {

    }

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
