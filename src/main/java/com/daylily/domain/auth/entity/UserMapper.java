package com.daylily.domain.auth.entity;

import com.daylily.domain.auth.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * .githubId(oAuth2User.getAttribute("id"))
     * .login(oAuth2User.getAttribute("login"))
     * .email(oAuth2User.getAttribute("email"))
     * .githubProfileUrl(oAuth2User.getAttribute("html_url"))
     * 명확한 이름으로 변경
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "githubId",           expression = "java(oAuth2User.getAttribute(\"id\"))")
    @Mapping(target = "githubUsername",     expression = "java(oAuth2User.getAttribute(\"login\"))")
    @Mapping(target = "email",              expression = "java(oAuth2User.getAttribute(\"email\"))")
    @Mapping(target = "githubProfileUrl",   expression = "java(oAuth2User.getAttribute(\"html_url\"))")
    User toEntity(OAuth2User oAuth2User);

    UserResponse toResponse(User user);
}
