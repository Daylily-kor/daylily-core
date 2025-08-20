package com.daylily.domain.github.service;

import com.daylily.domain.docker.client.DockerGrpcClient;
import com.daylily.domain.docker.dto.DockerMapper;
import com.daylily.domain.github.action_type.PullRequestActionType;
import com.daylily.domain.github.api.GitHubClientFactory;
import com.daylily.domain.github.entity.GitHubApp;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import com.daylily.domain.github.util.ActionTypeChecker;
import com.daylily.domain.github.util.PayloadParser;
import com.daylily.proto.build.GrpcImageBuildRequest;
import com.daylily.proto.build.GrpcImageBuildResponse;
import com.daylily.proto.run.GrpcContainerRunRequest;
import com.daylily.proto.run.GrpcContainerRunResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestHandlerServiceImpl implements PullRequestHandlerService {

    private final DockerGrpcClient dockerGrpcClient;
    private final PayloadParser payloadParser;
    private final DockerMapper dockerMapper;

    private final GitHubClientFactory gitHubClientFactory;
    private final GitHubAppRepository gitHubAppRepository;

    @Value("${app.base-domain}")
    private String baseDomain;

    @Override
    public void handlePullRequestEvent(String eventType, String rawPayload) {
        // 1. PR Payload 읽기
        GHEventPayload.PullRequest payload = payloadParser.parsePullRequest(rawPayload);

        // 2. PR Action Type에 따라 처리
        GHPullRequest pullRequest;
        PullRequestActionType actionType = ActionTypeChecker.fromPullRequestActionString(payload.getAction());
        switch (actionType) {
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
        GrpcImageBuildRequest imageBuildRequest = dockerMapper.toImageBuildRequest(pullRequest);
        GrpcImageBuildResponse imageBuildResponse = dockerGrpcClient.buildImage(imageBuildRequest);
        log.debug("Successfully build image: {}", imageBuildResponse.toString());

        // 4. 빌드된 이미지로 컨테이너 실행
        var commitSHAFull = pullRequest.getHead().getSha();
        var commitSHA = commitSHAFull.substring(0, 8);
        var containerName = "pr-%s-%s".formatted(pullRequest.getRepository().getName(), commitSHA);

        GrpcContainerRunResponse runResponse = dockerGrpcClient.runContainer(
                GrpcContainerRunRequest.newBuilder()
                        .setImageId(imageBuildResponse.getImageId())
                        .setContainerName(containerName)
                        .setCommitSHA(commitSHAFull)
                        .setBaseDomain(baseDomain)
                        .build()
        );
        log.debug("Successfully started container: {}", runResponse.toString());

        // 5. GitHub에 컨테이너 URL과 상태 업데이트
        GitHubApp gitHubApp = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        // Pull request에 댓글 달기
        GitHub gh = gitHubClientFactory.withAppInstallation(gitHubApp);
        try {
            // TODO: 컨테이너 URL들에 대해서는 https 가능한지 잘 모름
            var comment = """
                :rocket: **Container Started** :rocket:
                - **Container URL**: http://%s
                - **Container Status**: %s
                """.formatted(runResponse.getContainerUrl(), runResponse.getStatus());

            // owner/repo 형식
            var repositoryName = pullRequest.getRepository().getFullName();
            var prNumber = pullRequest.getNumber();

            // PR에 댓글 달기
            gh.getRepository(repositoryName)
                    .getPullRequest(prNumber)
                    .comment(comment);

            log.debug("Successfully commented on pull request {}: {}", prNumber, comment);
        } catch (IOException e) {
            log.error("Failed to comment on pull request: {}", e.getMessage());
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR);
        }
    }
}