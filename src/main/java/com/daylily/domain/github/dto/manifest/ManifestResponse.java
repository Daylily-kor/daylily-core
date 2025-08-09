package com.daylily.domain.github.dto.manifest;

import io.swagger.v3.oas.annotations.media.Schema;

public record ManifestResponse(
        @Schema(
                description = "The temporary state token used to prevent CSRF attacks.",
                example = "5a125273-c367-4bac-872d-34b3329e8523"
        )
        String state,
        Manifest manifest
) {
}
