package com.sprint.mission.discodeit.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    componentModel = "spring",                      // 스프링 빈으로 등록
    unmappedTargetPolicy = ReportingPolicy.IGNORE, // 매핑 되지 않는 필드 있어도 무시
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface GlobalMapperConfig {

}
