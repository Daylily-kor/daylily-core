package com.daylily.domain.docker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Docker 버전 정보를 담는 DTO 클래스
 */
public record ResponseDockerVersion(
    @Schema(example = "28.3.2")                             String version,
    @Schema(example = "1.51")                               String apiVersion,
    @Schema(example = "Docker Desktop 4.43.2 (199162)")     String platform,
    @Schema(example = "linux")                              String os,
    @Schema(example = "amd64")                              String arch,
    @Schema(example = "6.6.87.2-microsoft-standard-WSL2")   String kernelVersion
) {
    
}
