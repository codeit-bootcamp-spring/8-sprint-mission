package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {
    MessageMapper.class,
    ReadStatusMapper.class,
    UserMapper.class
})
public interface ChannelMapper {

  @Mapping(target = "participants")
  @Mapping(target = "lastMessageAt")
  ChannelDto toDto(Channel channel, List<User> participants, Instant lastMessageAt);
}