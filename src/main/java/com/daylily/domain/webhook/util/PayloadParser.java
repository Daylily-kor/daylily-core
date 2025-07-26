package com.daylily.domain.webhook.util;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;

@Component
@RequiredArgsConstructor
public class PayloadParser {

    private final GitHub gh;

    public GHEventPayload.PullRequest parsePullRequestPayload(String rawPayload) {
        try (var stringReader = new StringReader(rawPayload)) {
            return gh.parseEventPayload(stringReader, GHEventPayload.PullRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
