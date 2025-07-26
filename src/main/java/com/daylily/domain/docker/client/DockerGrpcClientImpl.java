package com.daylily.domain.docker.client;

import com.daylily.domain.docker.dto.DockerMapper;
import com.daylily.domain.docker.exception.DockerGrpcErrorCode;
import com.daylily.domain.docker.exception.DockerGrpcException;
import com.daylily.proto.build.GrpcImageBuildResponse;
import com.daylily.proto.run.GrpcContainerRunResponse;
import com.daylily.proto.service.DockerServiceGrpc.DockerServiceBlockingStub;
import com.daylily.proto.version.GrpcDockerVersionResponse;
import com.google.protobuf.Empty;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.daylily.domain.docker.dto.GrpcRequest.BuildImageRequest;
import static com.daylily.domain.docker.dto.GrpcRequest.RunContainerRequest;
import static com.daylily.domain.docker.dto.GrpcResponse.*;

@Slf4j
@Service
public class DockerGrpcClientImpl implements DockerGrpcClient {

    private final DockerMapper dockerMapper;

    // gRPC 클라이언트
    @Qualifier("grpcClient")
    private final DockerServiceBlockingStub client;

    public DockerGrpcClientImpl(DockerServiceBlockingStub client, DockerMapper dockerMapper) {
        this.dockerMapper = dockerMapper;
        this.client = client;
    }

    @Override
    public DockerVersionResponse getVersion() {
        try {
            GrpcDockerVersionResponse versionResponse = client.version(Empty.getDefaultInstance());
            return dockerMapper.toResponseDockerVersion(versionResponse);
        } catch (Exception e) {
            Status status = Status.fromThrowable(e);
            log.error("Failed to get version from Docker client: {}", status.getDescription());
            throw new DockerGrpcException(DockerGrpcErrorCode.of(status));
        }
    }

    @Override
    public BuildImageResponse buildImage(BuildImageRequest request) {
        var imageBuildRequest = dockerMapper.toImageBuildRequest(request);

        try {
            GrpcImageBuildResponse imageBuildResponse = client.imageBuild(imageBuildRequest);
            return dockerMapper.toImageBuildResponse(imageBuildResponse);
        } catch (Exception ex) {
            Status status = Status.fromThrowable(ex);
            log.error("Failed to build image: {}", status.getDescription());
            throw new DockerGrpcException(DockerGrpcErrorCode.of(status));
        }
    }

    @Override
    public RunContainerResponse runContainer(RunContainerRequest request) {
        var runRequest = dockerMapper.toRunRequest(request);

        try {
            GrpcContainerRunResponse runResponse = client.containerRun(runRequest);
            return dockerMapper.toRunContainerResponse(runResponse);
        } catch (Exception e) {
            Status status = Status.fromThrowable(e);
            log.error("Failed to run container: {}", status.getDescription());
            throw new DockerGrpcException(DockerGrpcErrorCode.of(status));
        }
    }
}
