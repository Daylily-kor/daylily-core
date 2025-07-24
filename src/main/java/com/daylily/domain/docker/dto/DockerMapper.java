package com.daylily.domain.docker.dto;

import com.daylily.proto.build.ImageBuildRequest;
import com.daylily.proto.version.VersionResponse;
import org.kohsuke.github.GHPullRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DockerMapper {

    DockerMapper INSTANCE = Mappers.getMapper(DockerMapper.class);
    
    ResponseDockerVersion toResponseDockerVersion(VersionResponse protoVersionResponse);

    /**
     * GHPullRequest 객체를 ImageBuildRequest로 변환합니다. <br/>
     * 저장소 이름, 브랜치, PR 번호, SHA를 포함합니다.
     * @param pr GHPullRequest 객체
     * @return ImageBuildRequest 객체
     */
    @Mapping(target = "repositoryName", source = "pr.repository.fullName")
    @Mapping(target = "ref",            source = "pr.head.ref")
    @Mapping(target = "prNumber",       source = "pr.number")
    @Mapping(target = "sha",            source = "pr.head.sha")
    ImageBuildRequest toImageBuildRequest(GHPullRequest pr);
}
