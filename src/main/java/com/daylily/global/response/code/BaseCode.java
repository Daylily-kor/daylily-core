package com.daylily.global.response.code;

import io.swagger.v3.oas.annotations.media.Schema;

public interface BaseCode {

    @Schema(example = "GLOBAL-200")
    String getCode();

    @Schema(example = "Request successful")
    String getMessage();

    @Schema(example = "200")
    int getStatus();
}
