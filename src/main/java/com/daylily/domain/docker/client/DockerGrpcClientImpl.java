package com.daylily.domain.docker.client;

import com.daylily.domain.docker.dto.DockerMapper;
import com.daylily.domain.docker.exception.DockerGrpcErrorCode;
import com.daylily.domain.docker.exception.DockerGrpcException;
import com.daylily.proto.build.ImageBuildResponse;
import com.daylily.proto.run.RunRequest;
import com.daylily.proto.run.RunResponse;
import com.daylily.proto.service.DockerServiceGrpc.DockerServiceBlockingStub;
import com.daylily.proto.version.VersionResponse;
import com.google.protobuf.Empty;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Slf4j
@Service
public class DockerGrpcClientImpl implements DockerGrpcClient {

    // gRPC 클라이언트
    @Qualifier("grpcClient")
    private final DockerServiceBlockingStub client;

    public DockerGrpcClientImpl(DockerServiceBlockingStub client) {
        this.client = client;
    }

    @Override
    public VersionResponse getVersion() {
        return executeGrpcCall(
                () -> client.version(Empty.getDefaultInstance()),
            "Failed to get version from Docker client"
        );
    }

    @Override
    public ImageBuildResponse buildImageFromPullRequest(GHPullRequest pullRequest) {
        var request = DockerMapper.INSTANCE.toImageBuildRequest(pullRequest);
        return executeGrpcCall(
                () -> client.build(request),
                "Failed to build image"
        );
    }

    @Override
    public RunResponse runContainer(String imageId, String containerName) {
        var request = RunRequest.newBuilder()
                .setImageId(imageId)
                .setContainerName(containerName)
                .build();
        return executeGrpcCall(
                () -> client.run(request),
                "Failed to run container"
        );
    }

    /**
     * gRPC 호출을 실행하고 예외를 처리하는 메소드
     * @param grpcCall gRPC 호출을 수행하는 람다식
     * @param errorMessage 오류 발생시 출력할 메시지
     * @return gRPC 호출 결과
     * @param <T> gRPC 호출 결과 타입
     */
    private <T> T executeGrpcCall(Supplier<T> grpcCall, String errorMessage) {
        try {
            // gRPC 호출 결과를 반환
            return grpcCall.get();
        } catch (Exception ex) {
            Status status = Status.fromThrowable(ex);
            log.error(errorMessage);
            throw new DockerGrpcException(DockerGrpcErrorCode.of(status));
        }
    }
}
