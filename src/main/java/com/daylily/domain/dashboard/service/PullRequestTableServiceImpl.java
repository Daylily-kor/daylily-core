package com.daylily.domain.dashboard.service;

import com.daylily.domain.dashboard.dto.ContainerStatusType;
import com.daylily.domain.dashboard.dto.PullRequestTableRow;
import com.daylily.domain.docker.client.DockerGrpcClient;
import com.daylily.domain.github.api.GitHubClientFactory;
import com.daylily.domain.github.entity.GitHubApp;
import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import com.daylily.domain.github.repository.GitHubAppRepository;
import com.daylily.proto.containerList.GrpcContainerResponse;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PullRequestTableServiceImpl implements PullRequestTableService {

    private final DockerGrpcClient dockerClient;
    private final GitHubAppRepository gitHubAppRepository;
    private final GitHubClientFactory gitHubClientFactory;

    @Override
    public Map<String, List<PullRequestTableRow>> getPullRequests() {
        GitHubApp app = gitHubAppRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new GitHubException(GitHubErrorCode.APP_NOT_FOUND));

        GitHub gh = gitHubClientFactory.withAppInstallation(app);

        // 저장소 별 열려있는 Pull Request 목록
        Map<GHRepository, List<GHPullRequest>> openPullRequestsMap = getOpenPullRequestsMap(gh);
        Map<String, GrpcContainerResponse> containersByCommitSHA = getContainersByCommitSHA();

        Map<String, List<PullRequestTableRow>> pullRequestsPerRepository = new HashMap<>();

        openPullRequestsMap.forEach((repository, pullRequests) -> {
            List<PullRequestTableRow> pullRequestTableRows = pullRequests.stream()
                    .map(pullRequest -> buildPullRequestTableRow(pullRequest, containersByCommitSHA))
                    .toList();

            pullRequestsPerRepository.put(repository.getFullName(), pullRequestTableRows);
        });
        return pullRequestsPerRepository;
    }

    private static PullRequestTableRow buildPullRequestTableRow(GHPullRequest pullRequest, Map<String, GrpcContainerResponse> containersByCommitSHA) {
        String commitSHA = pullRequest.getHead().getSha();
        GrpcContainerResponse containerResponse = containersByCommitSHA.getOrDefault(commitSHA, GrpcContainerResponse.getDefaultInstance());

        String containerUrl = containerResponse.getContainerUrl().isEmpty() ? null : containerResponse.getContainerUrl();
        ContainerStatusType containerStatusType = ContainerStatusType.fromString(containerResponse.getStatus());
        String containerStatus = containerResponse.getStatus().isEmpty() ? null : containerResponse.getStatus();
        try {
            return PullRequestTableRow.builder()
                    .url(pullRequest.getHtmlUrl().toURI())
                    .title(pullRequest.getTitle())
                    .number(pullRequest.getNumber())
                    .author(pullRequest.getUser().getLogin())
                    .state(pullRequest.getState().name().toLowerCase())
                    .createdAt(pullRequest.getCreatedAt().toString())
                    .updatedAt(pullRequest.getUpdatedAt().toString())
                    .containerUrl(containerUrl)
                    .containerState(containerStatusType)
                    .containerStatus(containerStatus)
                    .build();
        } catch (URISyntaxException | IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_API_ERROR);
        }
    }

    private static Map<GHRepository, List<GHPullRequest>> getOpenPullRequestsMap(GitHub gh) {
        return getRepositories(gh)
                .stream()
                .collect(Collectors.toMap(
                        repository -> repository,
                        PullRequestTableServiceImpl::getOpenPullRequests
                ));
    }

    private Map<String, GrpcContainerResponse> getContainersByCommitSHA() {
        List<GrpcContainerResponse> containersList = dockerClient.getPullRequestContainers().getContainersList();
        return containersList.stream()
                .collect(Collectors.toMap(
                        GrpcContainerResponse::getCommitSHA,
                        grpcContainerResponse ->  grpcContainerResponse
                ));
    }

    private static List<GHPullRequest> getOpenPullRequests(GHRepository repository) {
        try {
            return repository.queryPullRequests().state(GHIssueState.OPEN).list().toList();
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.GITHUB_LIST_REPOS_ERROR);
        }
    }

    private static List<GHRepository> getRepositories(GitHub gh) {
        try {
            return gh.getInstallation().listRepositories().toList();
        } catch (Exception e) {
            throw new GitHubException(GitHubErrorCode.REPOSITORY_LISTING_FAILED);
        }
    }
}
