package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public abstract class UserMapper {

  @Autowired
  protected SessionRegistry sessionRegistry;

  @Mapping(target = "online", expression = "java(isUserOnline(user.getUsername()))")
  public abstract UserDto toDto(User user);

  protected boolean isUserOnline(String username) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(p -> p instanceof UserDetails ud && ud.getUsername().equals(username))
        .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
        .anyMatch(sessionInfo -> !sessionInfo.isExpired());
  }
}
