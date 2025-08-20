package com.daylily.domain.docker.dto;

import com.daylily.proto.build.GrpcImageBuildRequest;
import com.daylily.proto.version.GrpcDockerVersionResponse;
import org.kohsuke.github.GHPullRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DockerMapper {

    GrpcResponse.DockerVersionResponse toResponseDockerVersion(GrpcDockerVersionResponse protoVersionResponse);

    @Mapping(target = "repositoryName", source = "request.repository.fullName")
    @Mapping(target = "ref",            source = "request.head.ref")
    @Mapping(target = "prNumber",       source = "request.number")
    @Mapping(target = "sha",            source = "request.head.sha")
    GrpcImageBuildRequest toImageBuildRequest(GHPullRequest request);
}
