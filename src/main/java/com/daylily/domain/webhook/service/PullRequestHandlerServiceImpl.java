package com.daylily.domain.webhook.service;

import com.daylily.domain.docker.client.DockerGrpcClient;
import com.daylily.domain.github.action_type.PullRequestActionType;
import com.daylily.domain.webhook.util.PayloadParser;
import com.daylily.proto.build.ImageBuildResponse;
import com.daylily.proto.run.RunResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHPullRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestHandlerServiceImpl implements PullRequestHandlerService {

    private final DockerGrpcClient dockerGrpcClient;
    private final PayloadParser payloadParser;

    @Override
    public void handlePullRequestEvent(String eventType, String rawPayload) {
        // 1. PR Payload 읽기
        GHEventPayload.PullRequest payload = payloadParser.parsePullRequestPayload(rawPayload);

        // 2. PR Action Type에 따라 처리
        GHPullRequest pullRequest;
        switch (PullRequestActionType.fromString(payload.getAction())) {
            // TODO: PR 컨테이너는 어떤 경우에 만들어야할지 논의
            // 새 PR 또는 PR에 새 커밋이 푸시된 경우에만 이미지를 빌드하고 컨테이너를 실행합니다.
            case EDITED: // TODO: PR의 Base 브랜치가 변경된 경우에도 이미지를 빌드해야할지 논의
            case OPENED, SYNCHRONIZE:
                pullRequest = payload.getPullRequest();
                break;
            default:
                return; // Ignore other actions
        }

        // 3. Docker gRPC 클라이언트로 이미지 빌드
        ImageBuildResponse buildResp = dockerGrpcClient.buildImageFromPullRequest(pullRequest);
        log.debug("Successfully build image: {}", buildResp.toString());

        // 4. 빌드된 이미지로 컨테이너 실행
        var commitSHA = pullRequest.getHead().getSha().substring(0, 8);
        var containerName = "pr-%s-%s".formatted(buildResp.getImageName(), commitSHA);
        RunResponse runResp = dockerGrpcClient.runContainer(buildResp.getImageId(), containerName);
        log.debug("Successfully started container: {}", runResp.toString());
    }
}