package com.daylily.domain.test.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TestResponse(
        @Schema(example = "Hello")  String name,
        @Schema(example = "World!") String description
) {
}
