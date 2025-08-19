package com.daylily.domain.github.dto.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

record HookAttributes(
        @Schema(example = "http://your-domain/api/webhook")
        String url,

        @Schema(example = "true")
        Boolean active
) {}

@Builder
public record Manifest(
        @Schema(example = "Your App Name")
        String name,

        @Schema(example = "http://your-domain")
        String url,

        @JsonProperty("hook_attributes")
        HookAttributes hookAttributes,

        @Schema(example = "http://your-domain/api/app/manifest/redirect")
        @JsonProperty("redirect_url")
        String redirectUrl,

        @JsonProperty("callback_urls")
        List<String> callbackUrls,

        @JsonProperty("setup_url")
        String setupUrl,

        @Schema(example = "Your App Description")
        String description,

        @Schema(example = "true")
        @JsonProperty("public")
        Boolean isPublic,

        @ArraySchema(
                schema = @Schema(example = "pull_request")
        )
        @JsonProperty("default_events")
        List<String> defaultEvents,

        @Schema(example = "{\"contents\": \"read\", \"pull_requests\": \"write\", \"metadata\": \"read\"}")
        @JsonProperty("default_permissions")
        Map<String, String> defaultPermissions,

        @Schema(example = "false")
        @JsonProperty("request_oauth_on_install")
        Boolean requestOauthOnInstall,

        @Schema(example = "false")
        @JsonProperty("setup_on_update")
        Boolean setupOnUpdate
) {
    public static Manifest withManifestRequest(ManifestRequest manifestRequest) {
        var webhookAttributes = new HookAttributes(
                manifestRequest.url() + "/api/webhook", // GitHubWebhookController::onEvent
                true
        );

        var url = manifestRequest.url();
        return Manifest.builder()
                .name(manifestRequest.name())
                .url(url)
                .hookAttributes(webhookAttributes)
                .redirectUrl(url + "/api/app/manifest/redirect") // GitHubAppController::createGitHubApp
                .callbackUrls(List.of(url + "/login/oauth2/code/github-app"))
                .setupUrl("")
                .description(manifestRequest.description())
                .isPublic(manifestRequest.isPublic())
                .defaultEvents(List.of("pull_request"))
                .defaultPermissions(Map.of(
                        "contents", "read",
                        "pull_requests", "write",
                        "metadata", "read"
                ))
                .requestOauthOnInstall(true)
                .setupOnUpdate(false)
                .build();
    }
}
