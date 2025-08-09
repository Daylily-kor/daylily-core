package com.daylily.domain.github.util;

import com.daylily.domain.github.exception.GitHubErrorCode;
import com.daylily.domain.github.exception.GitHubException;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;

@Component
@RequiredArgsConstructor
public class PayloadParser {

    public <T extends GHEventPayload> T parsePayload(String rawPayload, Class<T> clazz) {
        try (var stringReader = new StringReader(rawPayload)) {
            return GitHub.offline().parseEventPayload(stringReader, clazz);
        } catch (IOException e) {
            throw new GitHubException(GitHubErrorCode.INVALID_EVENT_PAYLOAD);
        }
    }

    public GHEventPayload.PullRequest parsePullRequest(String rawPullRequest) {
        return parsePayload(rawPullRequest, GHEventPayload.PullRequest.class);
    }

    public GHEventPayload.Installation parseInstallation(String rawInstallation) {
        return parsePayload(rawInstallation, GHEventPayload.Installation.class);
    }
}
