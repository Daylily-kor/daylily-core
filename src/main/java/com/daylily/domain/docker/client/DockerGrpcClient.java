package com.daylily.domain.docker.client;

import com.daylily.proto.build.GrpcImageBuildRequest;
import com.daylily.proto.build.GrpcImageBuildResponse;
import com.daylily.proto.containerList.GrpcContainerListResponse;
import com.daylily.proto.run.GrpcContainerRunRequest;
import com.daylily.proto.run.GrpcContainerRunResponse;
import com.daylily.proto.version.GrpcDockerVersionResponse;

public interface DockerGrpcClient {

    /**
     * 현재 실행 중인 Docker 버전을 조회합니다.
     */
    GrpcDockerVersionResponse getVersion();

    /**
     * 저장소를 Docker 이미지로 빌드합니다.
     */
    GrpcImageBuildResponse buildImage(GrpcImageBuildRequest request);

    /**
     * 지정된 Docker 이미지를 기반으로 컨테이너를 실행합니다.
     */
    GrpcContainerRunResponse runContainer(GrpcContainerRunRequest request);

    /**
     * 현재 실행 중인 Docker 컨테이너 목록을 조회합니다.
     */
    GrpcContainerListResponse getPullRequestContainers();
}
