package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class ChannelMapper {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ReadStatusRepository readStatusRepository;

  @Autowired
  private UserMapper userMapper;

  @Mapping(target = "participants", expression = "java(getParticipants(channel))")
  @Mapping(target = "lastMessageAt", expression = "java(getLastMessageAt(channel))")
  abstract public ChannelDto toDto(Channel channel);

  protected List<UserDto> getParticipants(Channel channel) {
    List<UserDto> participants = new ArrayList<>();
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      readStatusRepository.findAllByChannelId(channel.getId())
          .stream()
          .map(ReadStatus::getUser)
          .map(userMapper::toDto)
          .forEach(participants::add);
    }
    return participants;
  }

  protected Instant getLastMessageAt(Channel channel) {
    return messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
            channel.getId())
        .map(Message::getCreatedAt)
        .orElse(Instant.now());
  }
}
