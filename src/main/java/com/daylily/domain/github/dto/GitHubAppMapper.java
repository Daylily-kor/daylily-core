package com.daylily.domain.github.dto;

import com.daylily.domain.github.entity.GitHubApp;
import org.kohsuke.github.GHAppFromManifest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GitHubAppMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "appId",          source = "app.id")
    @Mapping(target = "installationId", ignore = true)
    GitHubApp toEntity(GHAppFromManifest app);
}
