package com.daylily.domain.docker.client;

import com.daylily.proto.build.ImageBuildResponse;
import com.daylily.proto.run.RunResponse;
import com.daylily.proto.version.VersionResponse;
import org.kohsuke.github.GHPullRequest;

public interface DockerGrpcClient {

    /**
     * 현재 실행 중인 Docker 버전을 조회합니다.
     */
    VersionResponse getVersion();

    /**
     * 저장소를 Docker 이미지로 빌드합니다.
     *
     * @param pullRequest GHPullRequest 객체로, PR의 저장소 이름과 브랜치 정보를 포함합니다.
     */
    ImageBuildResponse buildImageFromPullRequest(GHPullRequest pullRequest);

    /**
     * 지정된 Docker 이미지를 기반으로 컨테이너를 실행합니다.
     *
     * @param imageId       Docker 이미지 이름
     * @param containerName 컨테이너 이름
     */
    RunResponse runContainer(String imageId, String containerName);
}
