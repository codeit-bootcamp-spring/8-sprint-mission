package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReadStatusMapper {

  //자바 객체 구조를 따라감
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "channel.id", target = "channelId")
  ReadStatusDto toDto(ReadStatus readStatus);
}
