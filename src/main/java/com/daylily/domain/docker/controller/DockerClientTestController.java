package com.daylily.domain.docker.controller;

import com.daylily.domain.docker.client.DockerGrpcClient;
import com.daylily.domain.docker.dto.GrpcResponse.DockerVersionResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docker/test")
@RequiredArgsConstructor
@Tag(name = "Docker gRPC Test", description = "Docker gRPC 서버 테스트용 컨트롤러")
public class DockerClientTestController {
    
    private final DockerGrpcClient clientService;

    @GetMapping("/version")
    public ResponseEntity<DockerVersionResponse> getDockerVersion() {
        DockerVersionResponse resp = clientService.getVersion();
        return ResponseEntity.ok(resp);
    }
}
