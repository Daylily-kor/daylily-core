package com.daylily.domain.docker.client;

import static com.daylily.domain.docker.dto.GrpcRequest.BuildImageRequest;
import static com.daylily.domain.docker.dto.GrpcRequest.RunContainerRequest;
import static com.daylily.domain.docker.dto.GrpcResponse.*;

public interface DockerGrpcClient {

    /**
     * 현재 실행 중인 Docker 버전을 조회합니다.
     */
    DockerVersionResponse getVersion();

    /**
     * 저장소를 Docker 이미지로 빌드합니다.
     */
    BuildImageResponse buildImage(BuildImageRequest request);

    /**
     * 지정된 Docker 이미지를 기반으로 컨테이너를 실행합니다.
     */
    RunContainerResponse runContainer(RunContainerRequest request);
}
