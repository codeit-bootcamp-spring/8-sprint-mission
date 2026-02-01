package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public interface UserMapper {

  @Mapping(target = "online", source = "userStatus", qualifiedByName = "mapOnlineStatus")
  UserDto toDto(User user);

  //컴파일 시점에 MapStruct가 코드를 생성할 때 사용하는 tag
  @Named("mapOnlineStatus")
  default Boolean mapOnlineStatus(UserStatus userStatus) {
    if (userStatus == null) {
      return false;
    }
    return userStatus.isOnline();
  }
}
