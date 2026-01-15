package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.time.Instant;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = UserMapper.class
)
public abstract class ChannelMapper {

  protected final MessageRepository messageRepository;
  protected final ReadStatusRepository readStatusRepository;
  protected final UserMapper userMapper;

  protected ChannelMapper(MessageRepository messageRepository,
      ReadStatusRepository readStatusRepository,
      UserMapper userMapper) {
    this.messageRepository = messageRepository;
    this.readStatusRepository = readStatusRepository;
    this.userMapper = userMapper;
  }

  @Mapping(target = "lastMessageAt", expression = "java(resolveLastMessageAt(channel))")
  @Mapping(target = "participants", expression = "java(resolveParticipants(channel))")
  public abstract ChannelDto toDto(Channel channel);

  protected Instant resolveLastMessageAt(Channel channel) {
    return messageRepository.findFirstByChannelIdOrderByCreatedAtDesc(channel.getId())
        .map(m -> m.getCreatedAt())
        .orElse(null);
  }

  protected List<UserDto> resolveParticipants(Channel channel) {
    if (channel.getType() != ChannelType.PRIVATE) {
      return List.of();
    }

    return readStatusRepository.findUsersByChannelId(channel.getId()).stream()
        .map(userMapper::toDto)
        .toList();
  }

}
