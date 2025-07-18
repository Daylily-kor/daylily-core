package com.daylily.domain.test.dto;

import com.daylily.domain.test.entity.Test;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TestMapper {

    TestMapper INSTANCE = Mappers.getMapper(TestMapper.class);

    TestResponse toResponse(Test test);

    @Mapping(target = "id", ignore = true)
    Test toEntity(TestRequest request);
}
