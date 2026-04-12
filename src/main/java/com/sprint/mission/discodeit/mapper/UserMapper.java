package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = BinaryContentMapper.class)
@RequiredArgsConstructor
public abstract class UserMapper {

  protected JwtRegistry jwtRegistry;

  @Mapping(target = "online", expression = "java(isOnline(user))")
  public abstract UserDto toDto(User user);

  protected boolean isOnline(User user) {
    if (user == null || user.getId() == null) {
      return false;
    }

    return jwtRegistry.hasActiveJwtInformationByUserId(user.getId());
  }
}
