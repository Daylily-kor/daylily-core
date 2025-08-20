package com.daylily.domain.docker.client;

import com.daylily.domain.docker.exception.DockerGrpcErrorCode;
import com.daylily.domain.docker.exception.DockerGrpcException;
import com.daylily.proto.build.GrpcImageBuildRequest;
import com.daylily.proto.build.GrpcImageBuildResponse;
import com.daylily.proto.containerList.GrpcContainerListResponse;
import com.daylily.proto.run.GrpcContainerRunRequest;
import com.daylily.proto.run.GrpcContainerRunResponse;
import com.daylily.proto.service.DockerServiceGrpc.DockerServiceBlockingStub;
import com.daylily.proto.version.GrpcDockerVersionResponse;
import com.google.protobuf.Empty;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
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

    private <T> T wrapper(Supplier<T> supplier, String errorMessage) {
        try {
            return supplier.get();
        } catch (Exception e) {
            var status = Status.fromThrowable(e);
            log.error("{}: {}", errorMessage, status.getDescription());
            throw new DockerGrpcException(DockerGrpcErrorCode.of(status));
        }
    }

    @Override
    public GrpcDockerVersionResponse getVersion() {
        return wrapper(
                () -> client.version(Empty.getDefaultInstance()),
                "Failed to get Docker version"
        );
    }

    @Override
    public GrpcImageBuildResponse buildImage(GrpcImageBuildRequest request) {
        return wrapper(
                () -> client.imageBuild(request),
                "Failed to build image"
        );
    }

    @Override
    public GrpcContainerRunResponse runContainer(GrpcContainerRunRequest request) {
        return wrapper(
                () -> client.containerRun(request),
                "Failed to run container"
        );
    }

    @Override
    public GrpcContainerListResponse getPullRequestContainers() {
        return wrapper(
                () -> client.containerList(Empty.getDefaultInstance()),
                "Failed to get pull request containers"
        );
    }
}
