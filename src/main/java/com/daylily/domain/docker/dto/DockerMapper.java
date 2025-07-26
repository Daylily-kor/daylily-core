package com.daylily.domain.docker.dto;

import com.daylily.domain.docker.dto.GrpcResponse.BuildImageResponse;
import com.daylily.proto.build.GrpcImageBuildRequest;
import com.daylily.proto.build.GrpcImageBuildResponse;
import com.daylily.proto.run.GrpcContainerRunRequest;
import com.daylily.proto.run.GrpcContainerRunResponse;
import com.daylily.proto.version.GrpcDockerVersionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import static com.daylily.domain.docker.dto.GrpcRequest.*;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DockerMapper {

    GrpcResponse.DockerVersionResponse toResponseDockerVersion(GrpcDockerVersionResponse protoVersionResponse);

    @Mapping(target = "repositoryName", source = "pullRequest.repository.fullName")
    @Mapping(target = "ref",            source = "pullRequest.head.ref")
    @Mapping(target = "prNumber",       source = "pullRequest.number")
    @Mapping(target = "sha",            source = "pullRequest.head.sha")
    GrpcImageBuildRequest toImageBuildRequest(BuildImageRequest request);

    // 아래에 사용된 record들은 protobuf 파일로부터 컴파일된 Java 클래스의 필드명과 동일하여 수동 @Mapping이 필요하지 않습니다.

    BuildImageResponse toImageBuildResponse(GrpcImageBuildResponse response);

    GrpcContainerRunRequest toRunRequest(RunContainerRequest request);

    GrpcResponse.RunContainerResponse toRunContainerResponse(GrpcContainerRunResponse response);
}
