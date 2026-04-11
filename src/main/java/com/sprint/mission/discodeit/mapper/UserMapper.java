package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public interface UserMapper {
    @Mapping(target = "online", source = "isOnline")
    UserDto toDto(User user, boolean isOnline);
}
