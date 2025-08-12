package com.daylily.domain.github.dto.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ManifestRequest(
        @Schema(
                description = "The name of the GitHub App",
                example = "Daylily App"
        )
        @Size(min = 1, max = 33)
        String name,

        @Schema(
                description = "The description of the GitHub App",
                example = "A GitHub App for Daylily"
        )
        String description,

        @Schema(
                description = """
                        The domain URL where you hosted Daylily.<br/>
                        Webhooks will be sent to `/api/webhook` endpoint of this URL.
                        """,
                example = "https://daylily.app"
        )

        String url,

        @JsonProperty("public")
        @Schema(
                description = "Whether the GitHub App is public or private",
                example = "true"
        )
        Boolean isPublic
) {
}
