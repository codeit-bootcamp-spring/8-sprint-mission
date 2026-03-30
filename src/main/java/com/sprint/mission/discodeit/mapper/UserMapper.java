package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BinaryContentMapper.class)
public interface UserMapper {

  UserDto toDto(User user);
}
