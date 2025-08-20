package com.daylily.domain.github.dto;

import com.daylily.domain.github.entity.GitHubApp;
import com.daylily.global.exception.BaseException;
import com.daylily.global.response.code.ErrorCode;
import com.daylily.global.util.PemKeyConverter;
import org.kohsuke.github.GHAppFromManifest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GitHubAppMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "appId",          source = "app.id")
    @Mapping(target = "installationId", ignore = true)
    @Mapping(target = "pem",            source = "app.pem", qualifiedByName = "convertPemToPkcs8")
    GitHubApp toEntity(GHAppFromManifest app);

    /**
     * GitHub App 등록 시 발급받는 PEM 키를 PKCS#8 형식으로 변환합니다.
     */
    @Named("convertPemToPkcs8")
    default String convertToPkcs8(String pkcs1Pem) {
        if (pkcs1Pem == null) {
            return null;
        }

        try {
            return PemKeyConverter.convertPkcs1ToPkcs8(pkcs1Pem);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to convert PEM key format");
        }
    }
}
