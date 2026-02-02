package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.time.Instant;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

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
        .map(userMapper::toDto)
        .toList();
  }

}
