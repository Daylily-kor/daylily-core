package com.daylily.domain.docker.dto;

import lombok.Builder;
import org.kohsuke.github.GHPullRequest;

/**
 * gRPC 요청시 사용되는 DTO 클래스. Protobuf 파일로부터 컴파일된 Java 클래스로 맵핑됩니다.
 */
public abstract class GrpcRequest {

    public record BuildImageRequest(
            GHPullRequest pullRequest
    ) {

    }

    @Builder
    public record RunContainerRequest(
            String imageId,
            String containerName
    ) {

    }

}
