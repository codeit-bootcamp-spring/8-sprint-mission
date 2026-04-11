package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = UserMapper.class
)
public abstract class ChannelMapper {

  @Autowired
  protected MessageRepository messageRepository;

  @Autowired
  protected ReadStatusRepository readStatusRepository;

  @Autowired
  protected UserMapper userMapper;

  @Autowired
  protected SessionRegistry sessionRegistry;

  @Mapping(target = "lastMessageAt", expression = "java(resolveLastMessageAt(channel))")
  @Mapping(target = "participants", expression = "java(resolveParticipants(channel))")
  public abstract ChannelDto toDto(Channel channel);

  protected Instant resolveLastMessageAt(Channel channel) {
    return messageRepository.findFirstByChannel_IdOrderByCreatedAtDesc(channel.getId())
        .map(Message::getCreatedAt)
        .orElse(null);
  }

  protected List<UserDto> resolveParticipants(Channel channel) {
    if (channel.getType() != ChannelType.PRIVATE) {
      return List.of();
    }

    return readStatusRepository.findUsersByChannelId(channel.getId()).stream()
        .map(user -> userMapper.toDto(user, isUserOnline(user.getId())))
        .toList();
  }

  private boolean isUserOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(p -> p instanceof DiscodeitUserDetails)
        .map(p -> (DiscodeitUserDetails) p)
        .anyMatch(userDetails -> userDetails.getUserDto().id().equals(userId) &&
            !sessionRegistry.getAllSessions(userDetails, false).isEmpty());
  }

}
