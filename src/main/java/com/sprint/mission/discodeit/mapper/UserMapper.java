package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = BinaryContentMapper.class
)
public interface UserMapper {

  @Mapping(target = "online", source = "online")
  UserDto toDto(User user, boolean online);
}
