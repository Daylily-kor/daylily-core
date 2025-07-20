package com.daylily.domain.test.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record TestRequest(
        @Schema(example="이름 입력(1~20자)")
        @Size(min=1, max=20, message="이름은 1자 이상 20자 이하여야 합니다.")
        String name,

        @Schema(example="설명 입력")
        String description
) {
}
